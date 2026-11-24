# db_proj

A small DBMS written in Java: a recursive-descent command parser, one file per table, and an on-disk BST index per table.

## Requirements (no exceptions)

- Use recursive descent parsing for the grammar.
- Each table has its own file. The heading of the file contains the description of the table (# of attributes, attribute names, types and sizes). The tuples follow the header.
- Use a separate file to save the BST index for a table.
- Code must run in the COSC lab with only the software/libraries/add-ons already installed.

Full spec: `ProjectFiles/project.doc`. Test script: `ProjectFiles/project-test-data.txt`.

## Team

| Role | Name | GitHub |
|---|---|---|
| Partner A: Front End (parsing + queries) | Miles Padot| mpadot |
| Partner B: Back End (storage + indexing) | Harrison Mount | Harry-Stack256 |

---

## Architecture

```
 input text
     |
  [Lexer] -> tokens
     |
  [Parser] (recursive descent) -> Command objects (AST)
     |
  [Executor] -- uses --> [Catalog] (databases, schemas)
                    |--> [TableFile] (records on disk)
                    '--> [BSTIndex]  (keys -> record offsets, on disk)
```

Suggested layout:

```
src/
  Main.java                 // read-eval loop, INPUT/OUTPUT, EXIT
  lexer/    Token.java, TokenType.java, Lexer.java
  parser/   Parser.java, commands/*.java, Condition.java
  exec/     Executor.java, Evaluator.java, Printer.java
  catalog/  Catalog.java, Schema.java, Attribute.java
  storage/  TableFile.java, Record.java, BSTIndex.java
tests/
```

### Key rules from the spec (read before coding)

- Everything is **case-insensitive** (keywords, names, and string comparisons). `1 = 1.0` is true, so compare numbers as doubles.
- Statements may span lines and **end only at `;`**.
- **Primary key search** -> go through the BST, then use the record offset to read the record.
- **Non-key search on a table with a primary key** -> **in-order traversal of the BST**, reading records in that order, so everyone's output matches.
- Tables without a primary key can be scanned in any order.
- `DELETE` with no `WHERE` removes all tuples **and the table's schema**.
- `LET` builds a new table plus a BST on the given key. The key must be one of the selected attributes, otherwise error and abort.
- `SELECT` prints numbered rows with column headers, or `Nothing found`.
- Data and schemas must be saved on `EXIT` and reloaded on the next run.
- The test script has many **deliberate errors**. Every one needs a clear message and must not crash the program.

---

## Task Division

The split is by layer, so each of us can work and test independently. The two layers meet at the **interface contract** below. We agree on it first, then work in parallel.

### Partner A: Front End

| # | Task | Notes |
|---|---|---|
| A1 | BNF grammar (written in `docs/grammar.md`) | Do this first. One parser method per rule. |
| A2 | `Lexer` | Case-insensitive, whitespace/newline tolerant, identifiers, int/float/string constants, `RelOp`s, `;` |
| A3 | `Parser` (recursive descent) | All 11 commands, plus error reporting for malformed input |
| A4 | Command classes (AST) | `CreateTable`, `Insert`, `Select`, ... (these are the parser/executor contract) |
| A5 | `Condition` + `Evaluator` | `AttrName RelOp (Constant|AttrName)` with up to `and`/`or` chains, case-insensitive compare |
| A6 | `SELECT` (single table and multi-table joins) | Numbered output, headers, `Nothing found` |
| A7 | `Main` loop, `INPUT file [OUTPUT file]`, `EXIT` | Route all output through a `PrintStream`, not `System.out` |
| A8 | Aggregates: `count(*)`, `min`, `max`, `average` | Grad credit: each grad student implements and demos their own |

### Partner B: Back End

| # | Task | Notes |
|---|---|---|
| B1 | Record/file format design (written in `docs/storage.md`) | Header layout, fixed-size records, deleted-record handling |
| B2 | `Catalog` | `CREATE DATABASE`, `USE`, schema persistence, reload on startup |
| B3 | `TableFile` | Append, read-at-offset, update-at-offset, delete |
| B4 | `BSTIndex` (on disk) | Insert, search, **delete** (leaf, one child, two children), in-order traversal, own file |
| B5 | `CREATE TABLE`, `DESCRIBE`, `RENAME` | Validate: no duplicate attribute names, at least one attribute, PK handling |
| B6 | `INSERT` | Domain, key, and entity-integrity checks |
| B7 | `UPDATE`, `DELETE` | Keep the BST in sync, including updates that change the key |
| B8 | `LET` | Create a new table + BST from `SELECT` results (uses A6) |
| B9 | Aggregates (if B is a grad student) | See A8 |

### Shared

- Interface contract (below), agreed in the first session.
- Test data runs and bug triage.
- Final testing in the **COSC lab** before submission.

---

## Interface Contract

These signatures are a starting point. Change them together, never alone.

```java
// A -> B: what the parser produces (examples)
record CreateTable(String name, List<Attribute> attrs) {}
record Insert(String table, List<Object> values) {}
record Condition(...) {}            // tree of and/or over comparisons

// B -> A: what storage exposes
class TableFile {
    long insert(Record r);                     // returns offset
    Record read(long offset);
    void update(long offset, Record r);
    void delete(long offset);
}
class BSTIndex {
    void insert(Object key, long offset);
    Long find(Object key);                     // null if absent
    void delete(Object key);
    List<Long> inOrderOffsets();               // for non-key searches
}
class Catalog {
    void createDatabase(String name);
    void use(String name);
    Table getTable(String name);               // schema + TableFile + BSTIndex
}
```

---

## Milestones

| Phase | Partner A | Partner B | Done when |
|---|---|---|---|
| 0 | Grammar + AST classes | File format + interfaces | Contract agreed and committed |
| 1 | Lexer + parser for all commands | `TableFile`, `BSTIndex`, `Catalog` | Each tested alone with unit tests |
| 2 | Evaluator + `SELECT` | `CREATE`, `INSERT`, `DESCRIBE`, `RENAME` | Basic create/insert/select works end to end |
| 3 | Joins, `INPUT`/`OUTPUT`, `EXIT` | `UPDATE`, `DELETE`, `LET` | Full test file runs |
| 4 | Aggregates | Persistence checks | Save/reload works; error cases handled |
| 5 | Both | Both | Clean run in the COSC lab; submit |

---

## Workflow

- **Branches:** `main` always works. Use `feature/<name>` branches (e.g. `feature/lexer`, `feature/bst-delete`) and merge through pull requests. The other partner reviews before merging.
- **Commits:** small and focused, with messages like `Add BST in-order traversal`.
- **Tests:** unit-test the BST, lexer, and evaluator in isolation. For integration, run `project-test-data.txt` and compare against the expected output.
- **Decisions:** when the spec is ambiguous, write the assumption in `docs/assumptions.md` and confirm it with the instructor.

## Build and Run

_TODO: fill in once the COSC lab's Java version is confirmed._

```
javac -d out $(find src -name "*.java")
java -cp out Main
```

## Open Questions

- [ ] Which Java version is installed in the COSC lab?
- [ ] Is either of us taking the graduate-credit aggregates?
- [ ] Assumed integer size (16 or 32-bit)? The spec allows either.
- [ ] Output format when `INPUT ... OUTPUT file` is used: does the file also echo the commands?