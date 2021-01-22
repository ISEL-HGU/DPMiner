# DPMiner : Mining Repository Tool
### DPMiner is an integrated framework that can collect various types of data required for defect prediction through a single program.


**![](https://lh5.googleusercontent.com/bwjMBR5oG7lcSKPQsph9GTpT0OFa-JPFMTpQON_umhxzaqj4TEAMlBIVVyeeTjHe4XPRiZFhegHSqBpBSh7qzhZwsWsDu12_WY-TuDAJLae__nowPoRHhQU52irbmk4wB1YIRRs)**
## Contents of DPMiner

- [What is DPMiner](#what-is-DPMiner)
- [How to build Gradle](#how-to-build-Gradle)
- [Options](#options)

## What is DPMiner 

### 1. Repository list

![](https://lh4.googleusercontent.com/zYPwBbqpRVcsT3Qwl737KN8pncUSuIILx60DHDbR1gk-4vSwfJn8SWD5C1oxDIMg9HFH2DD5-0inKeiry8hS-9xMtOdUshfl38RWKwbAH29z_jkzJP32Q7kCrOrWbBEvC65tCv9InzU)

A list of repository URLs matching the conditions desired by the user is extracted from the version control system and the open source repository, GitHub.

To extract the URL list, DPminer use <u>**Search API**</u> among GitHub REST APIs. Search API provided by GitHub can receive a list of 100 repository URLs per page by sending information about conditions in query format. This framework can collect all of the project repository URLs corresponding to the condition by collecting a list of repository URLs for several queries.


> Possible conditions
>
> - commit Count Base
> - recent Date
> - fork Number
> - language Type
> - author Token


### 2. Patch

![](https://lh3.googleusercontent.com/2HEAr6r_uE5EUV_0hnG4AfYsQOlQZxbG25q-jAl34SPbb5m6X60TXM0dZV71UR_IJUpgG-BczbNfxllgOlJi0UQm8iUghbY0CU3jpyn9Tus1SA0LXWOiT6CHZjvL5oG56cBMMQ2ttyo)

The patch is function to collects bug fixing commit(BFC). There are three ways to collect bug fixing commit(BFC)

* **Jira**<br>
Jira is a repository for managing issues. Jira manages the project with a label indicating the nature of the issue and status information, which is the progress of the issue. DPMiner collects commit hash whose label is bug and progress status is Close.
[Find Jira key example](https://github.com/HGUISEL/BugPatchCollector/issues/5) 

* **GitHub Issue**<br>
GitHub provides an issue function for efficient project management. GitHub helps manage version upgrades, defect detection, and feature enhancements by assigning issue.  And the status of the issue is marked as open or closed. DPMiner collects data by considering the issue is a bug and the state is closed as BFC.

* **Commit message**<br>
Commit messages are recorded using keywords important to each commit for developers to efficiently maintain and collaborate. If there are "bug" and "fix" keywords in the commit message, that commit considers as BFC. DPMiner collects commit hash whose commit message have "bug" and "fix" keyword.


### 3. BIC

![](https://lh3.googleusercontent.com/snJMhnNZigWXnEZgN7ThanUpe5bFGsSDShlRB_4Lzl7KgWM7yZdwxK6n3jmibYdU10hmIdHPsQ1sE8gTEBHLxkPDLj1alrWDcrJoIbd5vIu2XxtUxVLTqfNEdBeKE1qd1gnXjJPv6Uk)

After collecting BFC (Bug Fix Commits) by the method described in Patch, BIC (Bug Introducing Commits) is collected by using SZZ algorithm. In this framework, two SZZ algorithm are used.

- **B-SZZ**
  The B-SZZ algorithm is an algorithm that finds the commit that introduced the bug by executing <u>git blame</u> on the modified line of the commit that fixed the bug. It is a basic szz algorithm.

- **AG-SZZ**
  The AG-SZZ algorithm uses <u>Annotation Graph</u> to correct blank lines, format changes, comments, and remove outlier BFCs that modify too many files at once. The annotation graph is created from the first commit to the commit that contains the defect correction information, and then the DFS algorithm is applied to the line where the defect is corrected to find the line causing the defect.
  
### 4. Metic

![](https://lh6.googleusercontent.com/WU16C8pIyqoshlu-GoXm7u4lqq7-xLOjSp84rq15vUHPNbsD0ySlDot0g_dcctTgUjmtX08asTkZ75bzyeCMhIBNEh7976iB-Sw3XrQl2ZFIsR8dYEveSZYxp-eZockRlClOqTRpb10)

The metric is information of source code for defect prediction.
* **Characteristic Vector**<br>
Characteristic Vector is a metric representing the structural change of the source code.

* **Bag of Words**<br>
Bag of Words is a metric that measures the frequency of occurrences of words after breaking up sentences into word units in source code and commit messages.

* **Meta data**<br>
Meta data consists of 25 types of data such as modified lines and added lines.



## How to build Gradle
<pre><code> $ ./gradlew distZip </code></pre>
or
<pre><code> $ gradle distZip </code></pre>
After the command, unzip "build/distributions/DPMiner.zip"<br>
The executable file is in build/distributions/DPMiner/bin<br>
There are two executable files. One is DPMiner.bat, the other is DPMiner.<br>
Window use DPMiner.bat, Linux or Mac OS use DPMiner.<br><br>

If you have trouble to build using gradlew, enter
<pre><code>$ gradle wrap</code></pre>


## Options
### Common options 

| Option | Description |
|:------:|:-----------:|
|   `-i*`   |  input path |
|   `-o*`   | output path |
* \* : `-i` and `-o`  are required.

### 1. Repository list

*Commend* : `findrepo`

| Option |    Description    |
|:------:|:-----------------:|
|   `-c`   |    create Date    |
|   `-cb`  | commit Count Base |
|   `-d`   |    recent Date    |
|   `-f`   |      fork Num     |
|   `-l`   |   language Type   |
| `-auth*` |     auth Token    |
* \* : `-auth` is required.

<pre><code> findrepo -l java -auth "Auth Token" </code></pre>
### 2. Patch

*Commend* : `patch`


| Option |               | Option |                                 |
|:------:|:-------------:|:------:|:-------------------------------:|
|  `-ij`  |    jira url   |  `-jk*`  |           jira keyword          |
|  `-ik`  |     commit message   |   `-k`   | bug keyword (default : bug,fix) |
|  `-ig`  | github issue |   `-l`   | issue bug label (default : big) |
* One of `-ij`, `-ik` and `-ig` is mandatory
* \* : `-jk` is required when using option `-ij`.

###### Jira example
<pre><code> //patch -i "Github URL" -o "local directory path"/"ProjectName"/patch -ij issues.apache.org -jk "Jira Key"
patch -i https://github.com/apache/juddi -o /Users/Desktop/juddi/patch -ij issues.apache.org -jk JUDDI </code></pre>
###### Github example (-l option)
<pre><code> //patch -i "Github URL" -o "local directory path"/"ProjectName"/patch -ig -l "issue keyword"
patch -i https://github.com/apache/camel-quarkus -o /Users/Desktop/camel-quarkus/patch -ig 
patch -i https://github.com/google/guava -o /Users/Desktop/camel-quarkus/patch -ig -l type=defect
</code></pre>
###### Commit message example (-k option)
<pre><code> //patch -i "Github URL" -o "local directory path"/"ProjectName"/patch -ik -k "bug keyword"
patch -i https://github.com/facebook/facebook-android-sdk -o /Users/Desktop/juddi/patch -ik
patch -i https://github.com/facebook/facebook-android-sdk -o /Users/Desktop/juddi/patch -ik -k help </code></pre>

### 3. BIC

*Commend* : `bic`
(Same with patch option table)

|  SZZ Option  |    Description   |
|:--------:|:----------------:|
|  `-z BSZZ` |     Git Blame (default)   |
| `-z AGSZZ` | Annotation Graph |
- `-z` option is not required.

###### Jira example (BSZZ)
<pre><code>  //bic -i "Github URL" -o "local directory path"/"ProjectName"/patch -ij issues.apache.org -jk "Jira Key"</code></pre>
###### Github example (BSZZ)
<pre><code>  //bic -i "Github URL" -o "local directory path"/"ProjectName"/patch -ig -l "issue keyword"</code></pre>
###### Commit message example (BSZZ)
<pre><code>  //bic -i "Github URL" -o "local directory path"/"ProjectName"/patch -ik -k "bug keyword"</code></pre>
###### AG-SZZ and B-SZZ example (Jira)
<pre><code> //bic -i "Github URL" -o "local directory path"/"ProjectName"/patch -ij issues.apache.org -jk "Jira Key" -z BSZZ
 //bic -i "Github URL" -o "local directory path"/"ProjectName"/patch -ij issues.apache.org -jk "Jira Key" -z AGSZZ</code></pre>
 

### 4. Metric

*Commend* : `metric`

|  Option  |    Description   |
|:--------:|:----------------:|
|  `-bp*` |     bic csv file path   |
- The metric can only be collected using file BIC_BSZZ.csv
###### Metric example
<pre><code> //metric  -i "Github URL" -o "local directory path"/metric -bp "BIC file path"/BIC_BSZZ_"ProjectName.csv"
metric  -i https://github.com/apache/juddi -o /Users/Desktop/metric -bp /Users/Desktop/BIC_BSZZ_juddi.csv </code></pre>
