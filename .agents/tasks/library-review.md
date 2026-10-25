# 図書館の貸出システム レビュー(読み取り専用調査)

対象: `c:\pleiades\2026-03\Java1\図書館の貸出システム`(兄弟の `図書館の貸出` は対象外)
調査日の環境: JDK 25.0.2 / javac 25.0.2、Windows、ビルドツールなし(素の javac)

---

## 1. 結論(先に回答)

| 観点 | 結果 |
|---|---|
| コンパイル | **通る**(エラー0、警告なし。`javac -encoding UTF-8` で10クラス生成) |
| 起動点 | **`Main.java`**(`public static void main` を持つのはここだけ)。`SystemMain` は画面ループ用のクラスで `main` なし |
| 基本フロー | ログイン→貸出→状態確認→検索→返却→終了まで**正常に動く** |
| 実務基準の点数 | **36 / 100** |
| 学習プロジェクトとしての点数 | **63 / 100** |
| 一言 | 「小さいが、ちゃんと動く」段階。継承・抽象クラス・enum・日付の使い方は良い。ただし**入力ミス1回で落ちる**、**状態表示が古いまま**、**延滞金なし**、**テストなし・保存なし**が大きい |

Rental_System3(DVD、実務40 / 学習62、コンパイルエラー3件)との比較:
- **「完成度」(動くか)は図書館の方が上**。コンパイルが通り、一通りの流れが最後まで動く。
- **機能量は Rental_System3 の方が多い**(Screen 系クラスが多い、料金/ランク等)。図書館は機能が少ない(登録・検索・貸出・返却・日付進行のみ、料金なし)。
- **設計の素直さは図書館が少し良い**。`Member`(抽象)→`Teacher`/`Student` のテンプレートメソッド、`Library`/`Manager`/`Book` の分担が小さく整理されている。ただし `Member` が `Scanner` を持って入出力までやっている点は両方に共通する弱点。
- 総合: 実務点は Rental_System3(約40)とほぼ同等かやや下(36)、学習点はほぼ同じ(63 対 62)。「動くのは図書館、機能が多いのは DVD」。
  ※Rental_System3 は今回ソースを読み直しておらず、依頼文にある過去の評価値との比較です。

---

## 2. 機能と構成(何をするシステムか)

ファイル一覧(`src` 以下):

- `Main.java` … `Scanner`/`Library`/`Manager` を作って `new SystemMain(sc,lib,manager).print()` を呼ぶ(エントリポイント)
- `SystemMain.java` … ログイン画面+メニューのループ(`print()`)、ログイン/新規作成(`getMember()`)
- `Member/Member.java` … 抽象クラス。`borrow()` `returnBook()` `updateStatus()` `search()` `display()` を持つ
- `Member/Teacher.java`(最大5冊・"先生")、`Member/Student.java`(最大3冊・"生徒")
- `Member/MemberStatus.java`(NORMAL / OVERDUE)、`Member/MemberRank.java`(NORMAL / GOLD(50))
- `Library/Library.java` … 蔵書15冊(id 100〜114)、現在日付 `today`、`nextDay()`、`memberSearch()`
- `Library/Book.java` … 題名・著者・id・貸出中フラグ・返却期限
- `Manager/Manager.java` … 会員リスト
- `メモ.java` … クラスではなくコメントだけのメモ(コンパイルに影響なし)

あるもの:
- 会員登録(名前+id入力で自動作成。同名なら「おかえりなさい」で再ログイン)
- 貸出(先生5冊/生徒3冊の上限、貸出中の本は不可、期限=貸出日+14日)
- 返却(自分の本のみ、遅れ日数表示、延滞→通常に戻ると通知)
- 検索(本の一覧表示と、id指定で著者表示)
- 延滞判定(期限が「今日より前」の本が1冊でもあれば OVERDUE で貸出不可)
- 日付を進める(メニュー6)
- 上限冊数まで借りると GOLD ランク

ないもの: 延滞金(罰金)、本の登録/削除、予約、会員の削除、ログイン認証、ファイル/DB 保存、テスト。

---

## 3. コンパイル結果

`javac -encoding UTF-8 -d <TEMP>\libclasses @<ソース一覧(BOMなし)>` → **終了コード0、エラー・警告なし**。
生成クラス: `Main, SystemMain, Library.Book, Library.Library, Manager.Manager, Member.Member, MemberRank, MemberStatus, Student, Teacher`。
(一時ファイルは調査後に削除済み。ソースは一切変更していません。)

---

## 4. 実行テスト(標準入力を流し込み、`java -cp <tmp> Main`)

システム日付は 2026-10-16 でした(`LocalDate.now()`)。

| # | 入力の意図 | 結果 |
|---|---|---|
| 1 | 生徒 taro で登録→100を借りる→状態確認→検索(100, NO)→100を返却→終了(-1) | **成功**。期限 2026-10-30、検索で「監督名は太郎です」、返却後は借り0冊 |
| 2 | 100を借りる→日付を16日進める→状態確認(5)→借りようとする→返却 | 借り直しは「延滞中の本があるので借りられない」で**正しくブロック**。返却で「2日遅れています / 通常客にもどれました」。**ただし日付を進めた直後の状態確認(5)は期限切れなのに `状態NORMAL` と表示**(古い値、バグ) |
| 3 | 3冊借りる→4冊目を試す→1冊返却 | 3冊目で GOLD に。4冊目は「もう、借りられない」。**1冊返して2冊になっても `ランク:GOLD` のまま**(降格処理なし。GOLD の特典も未使用) |
| 4 | 先生 sensei を作って-1 | 正常終了 |
| 5 | 存在しない本999を借りる→検索で `abc` | 999は「借りられませんでした」で正常。**`abc` で `NumberFormatException` により異常終了(exit=1)** |
| 6 | 100を借りる→検索で 115 / 100 / NO →返却で借りていないID 105 | 範囲外判定は動作。**借りていない本のIDを返却に入れると `NullPointerException` で異常終了** |
| 7 | ログイン後メニューで `abc` | **`InputMismatchException` で異常終了** |
| 8 | 借りていない状態で返却 | 「借りている、本はありません」で正常 |
| 9 | taro が100を借りて退館→先生 hanako が同じ100を借りる→同名再ログイン | 「その本は、すでに借りられています」と正しく拒否。同名で再ログインも動作 |

正常系はほぼ動作。**異常入力(数字以外・範囲外のID)に弱く、1回で落ちる**のが実測で確認できた最大の問題です。

---

## 5. 点数(軸別)

### 実務(プロダクション)基準: **36 / 100**

| 軸 | 配点 | 得点 | 理由 |
|---|---:|---:|---|
| OOP設計 | 15 | 8 | 抽象クラス+テンプレートメソッド(`getMaxBooks`/`getCallName`)は適切。enum も使えている。一方でドメインクラスが `Scanner`/`System.out` に依存(`Member.java` 全体)、ランク/ステータスが実質飾り |
| カプセル化 | 10 | 6 | フィールドは private、`unmodifiableList` で返している(`Manager.getMemberList`、`Library.getBookList`、`Member.getBookList`)。ただし `Book` は `setReturn()`/`setBorrowed()` を誰でも呼べ、貸出状態の整合性を `Member` が自前で操作している |
| 責務分離 | 15 | 6 | UI(入出力)・業務ルール・データが `Member`/`Library` に混在。`Library.memberSearch` が表示と入力を担当。`SystemMain.getMember` は先生/生徒でほぼ同じコードが2重 |
| 命名 | 10 | 5 | `setMemberList`(実際は add)、`setBorrowed`/`setReturn`(動詞が不自然)、`getDirec`(DVD時代の名残)、`memberSearch`、パッケージ名が `Library` とクラス名 `Library` で同名、パッケージが大文字始まり |
| バグリスク | 20 | 8 | クラッシュ3種(下記)、状態表示が古い、GOLD が降格しない、同名=同一人物のため別人でも衝突、検索の範囲 100〜114 が直書き |
| 入力検証 | 10 | 3 | `hasNextInt` で守っている箇所(ログイン選択、貸出ID、返却ID)はある。守っていない箇所(メニュー、id入力、検索)が多く、try/catch なし |
| テスト | 10 | 0 | 自動テストなし |
| 永続化 | 10 | 0 | 終了するとすべて消える |
| 合計 | 100 | **36** | |

### 学習プロジェクト基準(「継承・抽象クラス・ArrayList・enum まで習った初学者」): **63 / 100**

| 軸 | 配点 | 得点 | 理由 |
|---|---:|---:|---|
| OOP設計 | 15 | 12 | 継承+抽象メソッド+enum+`has-a`(Library が Book を持つ、Member が Library を持つ)が正しく使えている。ここは同レベルの学習者より上 |
| カプセル化 | 10 | 8 | private+getter、`unmodifiableList` を自分で入れている |
| 責務分離 | 15 | 9 | `Manager`/`Library`/`Member`/`SystemMain` に分けようとした意図が見える。入出力の混在はこの段階では許容範囲 |
| 命名 | 10 | 7 | 概ね読める。メソッド名のズレが数点 |
| バグリスク | 20 | 14 | 正常系は実測で通る。異常入力のクラッシュは「例外処理を習う前」なら理解できる |
| 入力検証 | 10 | 5 | `hasNextInt` ループは正しく書けている箇所がある |
| テスト | 10 | 3 | 未習なら許容。ただし手動テストの習慣は欲しい |
| 永続化 | 10 | 5 | 未習なら許容 |
| 合計 | 100 | **63** | |

(`.agents/tasks/oop-rescore.md` に過去の「OOP 9.5/15」の再採点メモがあり、本レビューの OOP 評価(学習基準 12/15)と近い方向。ただしそのメモは当時のコード=返却・検索が未実装の時点のものなので、現在のソースを直接読んで採点しました。)

---

## 6. 良い点(強み)

1. **継承の使い方が正しい**: `Member` が共通処理(`borrow`/`returnBook`)を持ち、`Teacher`/`Student` は上限冊数と呼び名だけを上書きする(`Member.java` の `getMaxBooks()`/`getCallName()`)。テンプレートメソッドの意図が分かる。
2. **日付を `LocalDate` で正しく扱っている**: `Book.setBorrowed(today)` で期限を `today.plusDays(14)`、返却時に `ChronoUnit.DAYS.between` で遅れ日数を計算。`Library.today` を持ち、`nextDay()` でテスト用に日付を進められる設計は実用的。
3. **延滞判定が仕様どおり動いた**(実測テスト2): 延滞中は貸出不可、返却で復帰、遅れ日数表示。
4. **コレクションを守っている**: `Collections.unmodifiableList` を3か所で使い、内部リストの直接変更を防いでいる。
5. **コンパイルが通り、正常系が最後まで動く**。コメント(`メモ.java` や Book.java 内の学習メモ)から、自分で考えて改善している様子が分かる。

---

## 7. 問題点 Top5(ファイル/行)

行番号は現在のソース基準の目安です(±数行)。

1. **異常入力で即クラッシュ(複数箇所)**
   - `Library.java:71` … `Integer.parseInt(s)` に `abc` → `NumberFormatException`(実測、exit=1)。同じ式を3回パースしている
   - `SystemMain.java:68` … メニュー選択の `sc.nextInt()` に `hasNextInt` ガードなし → `InputMismatchException`(実測)
   - `SystemMain.java:117,142` … id 入力の `nextInt()` も同様
2. **返却で「借りていない本のID」を入れると NullPointerException**: `Member.java:161`(`returnBook.getName()`)。検索ループで見つからず `returnBook == null` のまま使っている(実測)。ガードがないので、入力ミスが即クラッシュになる。
3. **状態表示が古い(ステータスのキャッシュ不整合)**: メニュー5(`SystemMain.java` の `case 5`)は `member.getStatus()` を表示するだけで `updateStatus()` を呼ばない。`updateStatus()` は借りる/返す時にしか実行されないので、日付を進めた直後は期限切れでも `NORMAL` と表示される(実測テスト2)。`status` は計算で出せる値なので、フィールドで持たず `isOverdue()` のようにその場で計算するのが安全。
4. **ドメインが入出力に結合し、責務が混在**: `Member`(`sc` を保持、`borrow`/`returnBook` 内で `System.out`/`Scanner`)、`Library.memberSearch(Scanner)`(`Library.java:~55-90`、しかも再帰ループ。入力を何千回も続けるとスタックオーバーフロー)。そのためテストが書けない。さらに `SystemMain.getMember` は先生/生徒でほぼ同一コードが重複(`SystemMain.java:~96-150`)。
5. **仕様の穴・名残り**:
   - `Library.java:82` … `"監督名は"` と表示し `getDirec()` を呼ぶ(DVD版のコピペ名残り。実際は著者)
   - `Library.java:71` … 範囲 `100`〜`114` が直書き。蔵書を増やすと壊れる
   - `Member.java:115` … GOLD になった後、返却しても降格しない。`MemberRank.getPoint()`(`MemberRank.java:14`)はどこからも呼ばれず、特典がない
   - 同名=同一人物(`SystemMain.java` の `getMember`)。id は未使用で重複チェックもなく、先生/生徒の区別も無視される
   - `Manager.setMemberList`(`Manager.java:19`)は実際には `add` なのに名前が setter(`メモ.java` に本人が気づいたメモあり)

---

## 8. 効果の大きい修正 Top3(実装はしていません)

1. **入力処理を1か所にまとめる(例: `InputHelper` クラス `readInt(prompt, min, max)`)**
   `hasNextInt()` ループ+`nextLine()` で読み、`SystemMain`/`Member`/`Library` の全 `nextInt()`/`parseInt` を置き換える。これだけで「1回の入力ミスで落ちる」問題(問題点1・2の入力側)が全部消える。Rental_System3 の `Screen/InputHelper.java` と同じ発想。返却は `returnBook == null` のとき「その本は借りていません」と戻す。
2. **状態を計算に変える+日付進行後に再評価**: `Member` に `isOverdue()` を作り(借りている本の `dueDate` が `library.getToday()` より前なら true)、`status` フィールドと `updateStatus()` をやめる。または `nextDay()` で全会員を再評価する。表示と貸出判定が常に一致するようになる。ついでに GOLD も「その時の借りている冊数」から計算するか、降格処理を入れる。
3. **入出力とルールを分ける**: `Member`/`Library` から `Scanner`/`System.out` を外し、`Library.lend(member, bookId)` / `returnBook(...)` が結果(成功/理由)を返し、表示は `SystemMain` 側だけにする。先生/生徒の重複した `getMember` もまとめる(`Map<String, Member>` で名前検索)。これで JUnit なしでも `main` から呼ぶテストが書け、次の段階(ファイル保存、延滞金)に進みやすくなる。

---

## 9. 学習者へのひとこと(正直な評価)

- 「動くものを作り切った」のは大きな成果です。Rental_System3 のようにコンパイルエラーが残っていないのは、この図書館システムの強み。
- 継承・抽象クラス・enum・`LocalDate` はきちんと使えています。次に伸ばすと一番効くのは**入力ミスで落ちないこと(例外処理 or 検証)**と、**画面の入出力とルールの分離**です。
- 「実務で通用する」には、まずテストと保存(ファイル)が必要で、そこが0点なので実務点は低めです。これは出来が悪いというより、まだ習っていない領域が空白という意味です。
- 次の一歩の推奨順: ①入力検証の共通化 → ②状態の計算化(バグ修正) → ③延滞金の追加(今ある遅れ日数計算を活かせる) → ④ファイル保存 → ⑤簡単なテスト。

---

## 付録: 調査方法

- 全ソース10ファイルを直接読み、行番号は grep で確認(行番号は目安)。
- コンパイル・実行は `%TEMP%` 以下に出力(ソース一覧は BOM なし UTF-8 の @argfile)。実行は9パターンの標準入力スクリプトを `java -cp` に流し、タイムアウト15秒を設定(全件タイムアウトなし)。終了後、一時ファイルはすべて削除。
- ソースコードは一切変更していません(書き込みはこのレポートのみ)。
