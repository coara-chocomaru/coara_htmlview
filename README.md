# Proc Info

ProcInfo は、Android のシステム情報（/proc 以下の各種情報）をユーザーアプリの範囲で取得して表示するためのシンプルなアプリケーションです。

## 特徴

- **サービス連携**  
  `ProcInfoService` とのバインドにより、バックグラウンドで非同期に情報を取得し、UI スレッドに結果を反映する設計です。
  
- **Proc CPU Info**  
  CPUに関する情報（処理能力や構成など）を表示します。

- **Proc Mem Info**  
  システムメモリの統計情報を提供します。

- **自己プロセスの詳細情報**  
  自身のプロセスに関する様々な統計や設定情報（Status、Maps、Mountinfo、Mounts、Mountstats、IO、Limits、OOM Score、OOM Adj など）を表示できます。

- **スケジューリング情報**  
  自プロセスのスケジューリング関連情報（Sched、Sched Boost、Sched Boost Period、Sched Group ID、Initial Task Load、Wake Up Idle、Schedstat、Smap）を確認できます。


## ライセンス
Apache License, Version 2.0
###
使用ライブリ等については下記のNOTICEをご覧ください。
#####
[NOTICE](./NOTICE.md)  
---
Copyright 2025 coara-chocomaru
