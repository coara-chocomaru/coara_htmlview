package com.coara.htmlview;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.coara.htmlview.databinding.ActivityHtmlviewBinding;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int TAG_COLOR = 0xFF0000FF;  
    private static final int ATTRIBUTE_COLOR = 0xFF008000; 
    private static final int VALUE_COLOR = 0xFFB22222; 

    private static final int LARGE_TEXT_THRESHOLD = 700;
    private static final int REQUEST_PERMISSION_WRITE = 100;
    private static final int REQUEST_CODE_PICK_HTML = 101;

    private ActivityHtmlviewBinding binding;

    private String originalHtml = "";
    private final Stack<String> editHistory = new Stack<>();
    private boolean isEditing = false;
    private volatile boolean isUpdating = false;
    private volatile boolean isLoading = false;
    private long lastUndoTimestamp = 0;
    private static final long UNDO_THRESHOLD = 1000;

    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern ATTR_PATTERN = Pattern.compile("(\\w+)=\\\"([^\\\"]*)\\\"");
    private final ArrayList<Integer> searchMatchPositions = new ArrayList<>();
    private int currentSearchIndex = -1;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private Runnable highlightRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHtmlviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.htmlEditText.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        binding.htmlEditText.setMovementMethod(new ScrollingMovementMethod());
        binding.htmlEditText.setTextIsSelectable(true); // 追加: テキスト選択を有効化（長押しでコピー&ペースト可能に）
        binding.htmlEditText.setKeyListener(null);

        binding.loadButton.setOnClickListener(v -> {
            String urlStr = binding.urlInput.getText().toString().trim();
            if ((urlStr.startsWith("http://") || urlStr.startsWith("https://")) && !isLoading) {
                fetchHtml(urlStr);
            } else if (isLoading) {
                Toast.makeText(MainActivity.this, "読み込み中です。", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "正しいURLを入力してください", Toast.LENGTH_SHORT).show();
            }
        });

        binding.loadFromStorageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/html");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "HTMLファイルを選択"), REQUEST_CODE_PICK_HTML);
        });

        binding.editButton.setOnClickListener(v -> {
            if (!isEditing) {
                editHistory.clear();
                editHistory.push(binding.htmlEditText.getText().toString());
                lastUndoTimestamp = System.currentTimeMillis();
                binding.htmlEditText.setKeyListener(new android.widget.EditText(MainActivity.this).getKeyListener());
                binding.htmlEditText.setFocusableInTouchMode(true);
                isEditing = true;
                Toast.makeText(MainActivity.this, "編集モードに入りました", Toast.LENGTH_SHORT).show();
            }
        });

        binding.htmlEditText.addTextChangedListener(new TextWatcher() {
            private String beforeChange;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isUpdating && isEditing) {
                    beforeChange = s.toString();
                }
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (highlightRunnable != null) {
                    uiHandler.removeCallbacks(highlightRunnable);
                }
            }
            @Override
            public void afterTextChanged(final Editable s) {
                if (!isUpdating && isEditing) {
                    final String newText = s.toString();
                    long now = System.currentTimeMillis();
                    if (now - lastUndoTimestamp > UNDO_THRESHOLD) {
                        editHistory.push(beforeChange);
                        lastUndoTimestamp = now;
                    }
                    highlightRunnable = () -> {
                        if (!isUpdating) {
                            isUpdating = true;
                            final String currentText = newText;
                            executor.execute(() -> {
                                final int[][] spans = getHighlightSpans(currentText);
                                uiHandler.post(() -> {
                                    applyHighlight(binding.htmlEditText.getText(), spans);
                                    isUpdating = false;
                                });
                            });
                        }
                    };
                    uiHandler.postDelayed(highlightRunnable, 150);
                }
            }
        });

        binding.revertFab.setOnClickListener(v -> {
            if (isEditing && !isUpdating && !editHistory.isEmpty()) {
                final String previousText = editHistory.pop();
                isUpdating = true;
                final Editable editable = binding.htmlEditText.getText();
                final int curPos = binding.htmlEditText.getSelectionStart();
                editable.replace(0, editable.length(), previousText);
                executor.execute(() -> {
                    final int[][] spans = getHighlightSpans(previousText);
                    uiHandler.post(() -> {
                        applyHighlight(binding.htmlEditText.getText(), spans);
                        int pos = Math.min(previousText.length(), curPos);
                        binding.htmlEditText.setSelection(pos);
                        isUpdating = false;
                        Toast.makeText(MainActivity.this, "変更を元に戻しました", Toast.LENGTH_SHORT).show();
                    });
                });
            } else if (isEditing && !isUpdating) {
                Toast.makeText(MainActivity.this, "これ以上前はありません", Toast.LENGTH_SHORT).show();
            }
        });

        binding.saveButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE);
            } else {
                saveHtmlToFile();
            }
        });

        binding.searchButton.setOnClickListener(v -> showSearchOverlay());

        binding.searchQueryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.searchNextButton.setOnClickListener(v -> moveToNextSearchMatch());
        binding.searchPrevButton.setOnClickListener(v -> moveToPreviousSearchMatch());
        binding.closeSearchButton.setOnClickListener(v -> hideSearchOverlay());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.htmlEditText.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (!isUpdating && binding.htmlEditText.getText().length() > LARGE_TEXT_THRESHOLD) {
                    final String currentText = binding.htmlEditText.getText().toString();
                    executor.execute(() -> {
                        final int[][] spans = getHighlightSpans(currentText);
                        uiHandler.post(() -> applyHighlight(binding.htmlEditText.getText(), spans));
                    });
                }
            });
        }
    }

    private void showSearchOverlay() {
        binding.searchOverlay.setVisibility(View.VISIBLE);
        binding.searchButton.setVisibility(View.INVISIBLE);
        binding.searchQueryEditText.requestFocus();
        binding.searchQueryEditText.setText("");
        binding.searchResultCountTextView.setText("件数: 0");
        searchMatchPositions.clear();
        currentSearchIndex = -1;
    }

    private void hideSearchOverlay() {
        binding.searchOverlay.setVisibility(View.GONE);
        binding.searchButton.setVisibility(View.VISIBLE);
        Editable text = binding.htmlEditText.getText();
        Object[] bgSpans = text.getSpans(0, text.length(), BackgroundColorSpan.class);
        for (Object span : bgSpans) {
            text.removeSpan(span);
        }
    }

    private void performSearch(final String query) {
        executor.execute(() -> {
            searchMatchPositions.clear();
            if (query != null && !query.isEmpty()) {
                String text = binding.htmlEditText.getText().toString();
                int index = text.indexOf(query, 0);
                while (index >= 0) {
                    searchMatchPositions.add(index);
                    index = text.indexOf(query, index + query.length());
                }
            }
            final int count = searchMatchPositions.size();
            uiHandler.post(() -> {
                binding.searchResultCountTextView.setText("件数: " + count);
                if (count > 0) {
                    currentSearchIndex = 0;
                    highlightCurrentSearchMatch();
                }
            });
        });
    }

    private void highlightCurrentSearchMatch() {
        Editable text = binding.htmlEditText.getText();
        Object[] bgSpans = text.getSpans(0, text.length(), BackgroundColorSpan.class);
        for (Object span : bgSpans) {
            text.removeSpan(span);
        }
        if (currentSearchIndex >= 0 && currentSearchIndex < searchMatchPositions.size()) {
            final int start = searchMatchPositions.get(currentSearchIndex);
            int end = start + binding.searchQueryEditText.getText().length();
            if (start >= 0 && end <= text.length()) {
                text.setSpan(new BackgroundColorSpan(Color.YELLOW), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                binding.htmlEditText.setSelection(start, end);
                binding.htmlEditText.post(() -> {
                    if (binding.htmlEditText.getLayout() != null) {
                        int line = binding.htmlEditText.getLayout().getLineForOffset(start);
                        int y = binding.htmlEditText.getLayout().getLineTop(line);
                        View parent = (View) binding.htmlEditText.getParent();
                        if (parent instanceof android.widget.ScrollView) {
                            ((android.widget.ScrollView) parent).smoothScrollTo(0, y);
                        } else {
                            binding.htmlEditText.scrollTo(0, y);
                        }
                    }
                });
            }
        }
    }

    private void moveToNextSearchMatch() {
        if (!searchMatchPositions.isEmpty()) {
            currentSearchIndex = (currentSearchIndex + 1) % searchMatchPositions.size();
            highlightCurrentSearchMatch();
        }
    }

    private void moveToPreviousSearchMatch() {
        if (!searchMatchPositions.isEmpty()) {
            currentSearchIndex = (currentSearchIndex - 1 + searchMatchPositions.size()) % searchMatchPositions.size();
            highlightCurrentSearchMatch();
        }
    }

    private void fetchHtml(final String urlString) {
        isLoading = true;
        executor.execute(() -> {
            final StringBuilder result = new StringBuilder(8192); 
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setUseCaches(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append('\n');
                }
                reader.close();
                uiHandler.post(() -> {
                    originalHtml = result.toString();
                    isEditing = false;
                    editHistory.clear();
                    Editable editable = binding.htmlEditText.getText();
                    editable.clear();
                    editable.append(originalHtml);
                    executor.execute(() -> {
                        final int[][] spans = getHighlightSpans(originalHtml);
                        uiHandler.post(() -> {
                            applyHighlight(binding.htmlEditText.getText(), spans);
                            binding.htmlEditText.setKeyListener(null);
                            isLoading = false;
                        });
                    });
                });
            } catch (final Exception e) {
                uiHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "HTMLの取得に失敗しました: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isLoading = false;
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_HTML && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                readHtmlFromUri(uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void readHtmlFromUri(final Uri uri) {
        isLoading = true;
        executor.execute(() -> {
            final StringBuilder sb = new StringBuilder(8192); 
            try {
                ContentResolver resolver = getContentResolver();
                try (InputStream in = resolver.openInputStream(uri)) {
                    if (in == null) {
                        throw new Exception("InputStreamが取得できません");
                    }
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append('\n');
                        }
                    }
                }
                uiHandler.post(() -> {
                    originalHtml = sb.toString();
                    isEditing = false;
                    editHistory.clear();
                    Editable editable = binding.htmlEditText.getText();
                    editable.clear();
                    editable.append(originalHtml);
                    executor.execute(() -> {
                        final int[][] spans = getHighlightSpans(originalHtml);
                        uiHandler.post(() -> {
                            applyHighlight(binding.htmlEditText.getText(), spans);
                            binding.htmlEditText.setKeyListener(null);
                            isLoading = false;
                        });
                    });
                });
            } catch (final Exception e) {
                uiHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "HTMLファイルの読み込みに失敗しました: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isLoading = false;
                });
            }
        });
    }

    private int[][] getHighlightSpans(String text) {
        ArrayList<int[]> spansList = new ArrayList<>();
        boolean isLargeText = text.length() > LARGE_TEXT_THRESHOLD && binding.htmlEditText.getLayout() != null;
        int visibleStart = 0;
        int visibleEnd = text.length();
        if (isLargeText) {
            int firstVisibleLine = binding.htmlEditText.getLayout().getLineForVertical(binding.htmlEditText.getScrollY());
            int lastVisibleLine = binding.htmlEditText.getLayout().getLineForVertical(binding.htmlEditText.getScrollY() + binding.htmlEditText.getHeight());
            visibleStart = binding.htmlEditText.getLayout().getLineStart(firstVisibleLine);
            visibleEnd = binding.htmlEditText.getLayout().getLineEnd(lastVisibleLine);
            text = text.substring(Math.max(0, visibleStart), Math.min(text.length(), visibleEnd));
        }
        Matcher tagMatcher = TAG_PATTERN.matcher(text);
        while (tagMatcher.find()) {
            int tagStart = (isLargeText ? visibleStart : 0) + tagMatcher.start();
            int tagEnd = (isLargeText ? visibleStart : 0) + tagMatcher.end();
            spansList.add(new int[]{tagStart, tagEnd, TAG_COLOR});
            String tagText = text.substring(tagMatcher.start(), tagMatcher.end());
            Matcher attrMatcher = ATTR_PATTERN.matcher(tagText);
            while (attrMatcher.find()) {
                int attrNameStart = tagStart + attrMatcher.start(1);
                int attrNameEnd = tagStart + attrMatcher.end(1);
                spansList.add(new int[]{attrNameStart, attrNameEnd, ATTRIBUTE_COLOR});
                int attrValueStart = tagStart + attrMatcher.start(2);
                int attrValueEnd = tagStart + attrMatcher.end(2);
                spansList.add(new int[]{attrValueStart, attrValueEnd, VALUE_COLOR});
            }
        }
        int[][] result = new int[spansList.size()][3];
        spansList.toArray(result);
        return result;
    }

    private void applyHighlight(Editable editable, int[][] spans) {
        if (spans != null) {
            Object[] oldSpans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);
            for (Object span : oldSpans) {
                editable.removeSpan(span);
            }
            int length = editable.length();
            for (int[] span : spans) {
                int start = span[0];
                int end = span[1];
                int color = span[2];
                if (start >= 0 && end <= length && start < end) {
                    editable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }

    private void saveHtmlToFile() {
        final String currentText = binding.htmlEditText.getText().toString();
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        final String fileName = timeStamp + (!currentText.equals(originalHtml) ? "Edit.html" : ".html");
        executor.execute(() -> {
            try {
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadDir, fileName);
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                    bos.write(currentText.getBytes(StandardCharsets.UTF_8));
                    bos.flush();
                }
                uiHandler.post(() -> Toast.makeText(MainActivity.this, "保存しました: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show());
            } catch (final Exception e) {
                uiHandler.post(() -> Toast.makeText(MainActivity.this, "保存に失敗しました: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_WRITE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveHtmlToFile();
        } else if (requestCode == REQUEST_PERMISSION_WRITE) {
            Toast.makeText(this, "書き込み権限が必要です", Toast.LENGTH_SHORT).show();
        }
    }
}
