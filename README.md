# DPMiner : Mining Repository Tool
DPMiner parses bug commits and saves them as .csv files. To know the bug commit, we need to get information about the commit. (수정)


**![](https://lh5.googleusercontent.com/bwjMBR5oG7lcSKPQsph9GTpT0OFa-JPFMTpQON_umhxzaqj4TEAMlBIVVyeeTjHe4XPRiZFhegHSqBpBSh7qzhZwsWsDu12_WY-TuDAJLae__nowPoRHhQU52irbmk4wB1YIRRs)**
## Contents of DPMiner

- [What is DPMiner](#what-is-DPMiner)
- [How to build Gradle](#how-to-build-Gradle)
- [Options](#options)

## What is DPMiner 

### 1. Repository list

![](https://lh4.googleusercontent.com/zYPwBbqpRVcsT3Qwl737KN8pncUSuIILx60DHDbR1gk-4vSwfJn8SWD5C1oxDIMg9HFH2DD5-0inKeiry8hS-9xMtOdUshfl38RWKwbAH29z_jkzJP32Q7kCrOrWbBEvC65tCv9InzU)

repository list 설명...

### 2. Patch

![](https://lh3.googleusercontent.com/2HEAr6r_uE5EUV_0hnG4AfYsQOlQZxbG25q-jAl34SPbb5m6X60TXM0dZV71UR_IJUpgG-BczbNfxllgOlJi0UQm8iUghbY0CU3jpyn9Tus1SA0LXWOiT6CHZjvL5oG56cBMMQ2ttyo)

patch 설명....


* **Jira**<br>
지라는 프로젝트를 진행하기 위해 필요한 다양한 기능을 제공합니다. 그중에 하나가 이슈 관리입니다. 지라의 이슈 관리 페이지에서는 버그와 관련된 이슈들만 따로 검색할 수 있고, 찾은 내용을 다운로드 받을 수 있습니다. [Find Jira key example](https://github.com/HGUISEL/BugPatchCollector/issues/5) <br>**지라에서 얻어낸 리퍼런스를 통해 프로젝트 내의 버그 정보를 수집하기 위해서는 -j과 -p 옵션을 함께 포함해야 합니다.** -j에는 Jira URL의 앞부분이 들어가고, -p에는 Project Key가 들어갑니다.

* **GitHub Issue**<br>
깃허브도 지라와 같이 버그 이슈들을 관리할 때 버그 라벨을 붙혀서 관리합니다. 이 프로그램은 **-j과 -p 옵션을 포함하지 않은 경우** 깃허브에서 버그 라벨이 붙은 이슈들을 검색해서 버그 정보를 수집합니다. 깃허브에서 옵션을 추가하지 않았을 때 기본적으로 제공하는 버그 라벨은 'bug'입니다. 그래서 초기 검색 버그 라벨은 'bug'이지만, 어떤 프로젝트에서는 버그 분류 라벨을 다르게 지정했기 때문에 이것에 대처하기 위해서는 -l 옵션이 필요합니다. 만약 프로젝트에서 버그 이슈들을 정리할 때 다른 bug 라벨을 사용했을 경우에는 -l 옵션을 통해 검색하는 버그 라벨의 이름을 변경 할 수 있습니다. (예를 들어 -l "buggy") 하지만 깃허브에 이슈 페이지가 존재하지 않는 프로젝트도 존재합니다. 이런 경우에는 자동으로 커밋 메세지를 분석해서 bug,resolved,fix와 같은 키워드가 존재하면 버그라고 판단합니다.

* **Commit message**<br>


### 3. BIC

![](https://lh3.googleusercontent.com/snJMhnNZigWXnEZgN7ThanUpe5bFGsSDShlRB_4Lzl7KgWM7yZdwxK6n3jmibYdU10hmIdHPsQ1sE8gTEBHLxkPDLj1alrWDcrJoIbd5vIu2XxtUxVLTqfNEdBeKE1qd1gnXjJPv6Uk)

BIC 설명~~ (jira와 github, commit message 설명은 위에서 했으니 BFC에서 szz알고리즘을 통하여 BIC를 파싱하는 부분 설명)
### 4. Metic

![](https://lh6.googleusercontent.com/WU16C8pIyqoshlu-GoXm7u4lqq7-xLOjSp84rq15vUHPNbsD0ySlDot0g_dcctTgUjmtX08asTkZ75bzyeCMhIBNEh7976iB-Sw3XrQl2ZFIsR8dYEveSZYxp-eZockRlClOqTRpb10)

Metric 설명 ~~

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

<pre><code> repository example </code></pre>
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
