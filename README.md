# lucene-testing
Basic command line for testing Lucene. This is mainly used to produce test cases and output for
[lucene-rust](https://github.com/dacut/lucene-rust).

# Building
To build a standalone JAR file, run `./gradlew jar`. The resulting JAR file will be in
`app/build/libs/lucene-testing.jar`.

# Running
After creating the standalone JAR, run it with:  
`java -jar ./app/build/libs/lucene-testing.jar [options] <subcommand> [subcommand arguments]`

# Examples

## Populating an index from a single document

This will create a Lucene database at `/tmp/my-index` containing a single document, _Twelfth Night_, by William
Shakespeare, from Project Gutenberg.

<code>% <b>curl -s -o twelfth-night.txt https://www.gutenberg.org/files/1526/1526-0.txt</b></code><br>
<code>% <b>java -jar ./app/build/libs/lucene-testing.jar -i /tmp/my-index add-document twelfth-night.txt</b></code><br>
<code>Added document twelfth-night.txt</code><br>

## Populating an index from multiple documents

This will add the current IETF RFCs to the Lucene database at `/tmp/my-index`. Any file without a `.txt` extension
will be ignored (this is hard-coded in the code currently).

```bash
% curl -s -o rfc-all.zip https://www.rfc-editor.org/in-notes/tar/RFC-all.zip
% java -jar ./app/build/libs/lucene-testing.jar -i /tmp/my-index add-archive rfc-all.zip
Added document a/ftp/in-notes/tar/readme.txt
Added document rfc-index.txt
Added document rfc1000.txt
Added document rfc1001.txt
Added document rfc1002.txt
...
Added document rfc998.txt
Added document rfc999.txt
Added document rfc99.txt
Added archive rfc-all.zip
```

## Querying

Examples of querying the database. These queries look in the `text` field, which contains the text of the document.

```
% java -jar ./app/build/libs/lucene-testing.jar -i /tmp/my-index query 'java+ipv6'
Result 1: document id 7273, score 4.8452883, filename rfc7703.txt, archive-filename rfc-all.zip, size 129620, compressed-size 14249, last-modified-time 20151119004949, compression-method deflated
     . . . . . . . . . . . . . . . . . . .   6
           2.2.1.  MAP-T Core  . . . . . . . . . . . . . . . . . . . . .   6
           2.2.2.  IPv6

Result 2: document id 1627, score 4.121941, filename rfc2471.txt, archive-filename rfc-all.zip, size 8274, compressed-size 3000, last-modified-time 19981203192137, compression-method deflated



                        IPv6 Testing Address Allocation

    Status of this Memo

       This memo defines

Result 3: document id 1888, score 4.080323, filename rfc2713.txt, archive-filename rfc-all.zip, size 41924, compressed-size 10639, last-modified-time 19991022162727, compression-method deflated
     for Representing Java(tm) Objects in an LDAP Directory

    Status of this Memo

       This memo

Result 4: document id 7994, score 4.029427, filename rfc8353.txt, archive-filename rfc-all.zip, size 210181, compressed-size 47051, last-modified-time 20180521233813, compression-method deflated

                                                                    May 2018


          Generic Security Service API Version 2: Java Bindings

% java -jar ./app/build/libs/lucene-testing.jar -i /tmp/my-index query valentine
Result 1: document id 0, score 5.1675396, filename twelfth-night.txt
     Street before Olivia’s House.


     Dramatis Personæ

    ORSINO, Duke of Illyria.
    VALENTINE, Gentleman

Result 2: document id 2294, score 3.265996, filename rfc3083.txt, archive-filename rfc-all.zip, size 90841, compressed-size 15826, last-modified-time 20010314184442, compression-method deflated

                  Co-chairs: Richard Woundy, rwoundy@cisco.com
```

To query a different field, use the `-f` flag:
```
% java -jar ./app/build/libs/lucene-testing.jar -i /tmp/my-index -f filename query 'rfc*822'
Result 1: document id 911, score 1.0, filename rfc1822.txt, archive-filename rfc-all.zip, size 2779, compressed-size 1082, last-modified-time 19950811221222, compression-method deflated
Result 2: document id 2005, score 1.0, filename rfc2822.txt, archive-filename rfc-all.zip, size 113554, compressed-size 30570, last-modified-time 20010418154345, compression-method deflated
Result 3: document id 3098, score 1.0, filename rfc3822.txt, archive-filename rfc-all.zip, size 23478, compressed-size 7164, last-modified-time 20040719233034, compression-method deflated
Result 4: document id 4173, score 1.0, filename rfc4822.txt, archive-filename rfc-all.zip, size 55063, compressed-size 15934, last-modified-time 20070226210143, compression-method deflated
Result 5: document id 6309, score 1.0, filename rfc6822.txt, archive-filename rfc-all.zip, size 31041, compressed-size 9042, last-modified-time 20121222011941, compression-method deflated
Result 6: document id 7405, score 1.0, filename rfc7822.txt, archive-filename rfc-all.zip, size 16210, compressed-size 4518, last-modified-time 20160331004240, compression-method deflated
Result 7: document id 7857, score 1.0, filename rfc822.txt, archive-filename rfc-all.zip, size 109200, compressed-size 31616, last-modified-time 19911017174717, compression-method deflated
Result 8: document id 8505, score 1.0, filename rfc8822.txt, archive-filename rfc-all.zip, size 16495, compressed-size 5849, last-modified-time 20210417192051, compression-method deflated
```

## Showing Lucene internals

Have some fun with the `test-lucene-read-apis` subcommand. This is used to generate test cases for `rust-lucene`.

```
% java -jar ./app/build/libs/lucene-testing.jar -i /tmp/my-index test-lucene-read-apis
SegmentInfos:
    CommitLuceneVersion: 9.5.0
    Generation: 3
    Id: 46bc03d2921e4d378a855b6e2d84e6e7
    LastGeneration: 3
    MinSegmentLuceneVersion: 9.5.0
    SegmentsFileName: segments_3
    TotalMaxDoc: 9164
    Size: 13
    Version: 30
    UserData: <empty>

    SegmentCommitInfo:
        Codec: Lucene95
        DelCount: 0 Soft=0
        DelGen: Current=-1 Next=1
        DocValuesGen: Current=-1 Next=1
        FieldInfosGen: Current-1 Next=1
        Id: 8e9f5c936b77e86a910b6ca68979fdc4
        MinVersion: 9.5.0
        MaxDoc: 1
        Version: 9.5.0
        Files:
            _0.cfe
            _0.si
            _0.cfs
        Attributes:
            Lucene90StoredFieldsFormat.mode: BEST_SPEED
        Diagnostics:
            lucene.version: 9.5.0
            source: flush
            timestamp: 1677040955926
            java.runtime.version: 17.0.6+10-LTS
            os: Mac OS X
            java.vendor: Amazon.com Inc.
            os.arch: aarch64
            os.version: 13.2.1
        IndexSort: <null>

    SegmentCommitInfo:
        Codec: Lucene95
    ...
CompositeReaderContext(CompositeReaderContext): id=828441346 docBaseInParent=0 ordInParent=0 isTopLevel=true
    Children: 13
        LeafReaderContext(LeafReaderContext): id=1698097425 docBaseInParent=0 ordInParent=0 isTopLevel=false docBase=0 ord=0
            Children: null
            Leaves: null
            LeafReader(SegmentReader): maxdoc=1 numdocs=1 deletions=0 createdMajorVersion=9 minVersion=9.5.0
        LeafReaderContext(LeafReaderContext): id=1335298403 docBaseInParent=1 ordInParent=1 isTopLevel=false docBase=1 ord=1
            Children: null
            Leaves: null
            LeafReader(SegmentReader): maxdoc=701 numdocs=701 deletions=0 createdMajorVersion=9 minVersion=9.5.0
        ...
```
