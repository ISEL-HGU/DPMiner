# BugPatchCollector : Bug commit mining tool
버그툴은 버그 커밋을 파싱해 csv파일로 저장한다. 버그 커밋을 알기 위해서는 커밋에 대한 정보를 얻어야 한다. 그러기 위한 방법

##### BugPatchCollector can be used in two Software Archives
* Jira
* Github


# Options
>Must required options 
* [-i](https://github.com/HGUISEL/BugPatchCollector/issues/7) (Input Option)
* -o (Directory Path of Result File Option)
>The other options
* [-r](https://github.com/HGUISEL/BugPatchCollector/issues/5) (Reference Option)
* [-l](https://github.com/HGUISEL/BugPatchCollector/issues/7) (Label Option)
* [-t](https://github.com/HGUISEL/BugPatchCollector/issues/8) (Thread Option)
* -x (Max lines of Modified line Option)
* -m (Min lines of Modified line Option)
* -h (Help Option)


# Jira
지라를 통해 어떻게 버그 커밋을 파싱하는지 설명


# Github
깃허브를 통해 ....


# Input Example
1. GitHub example
<pre><code> -o /Users/lamb0711/Documents/git/BugPatchCollector -i https://github.com/apache/incubator-dubbo -x 5 -m 3 -l type/bug </code></pre>
2. Jira example


# Output Example
The result file consist of Project Name, Short Commit Message, Commit Number, Date, Author and Patch.
* [CSV](https://github.com/HGUISEL/BugPatchCollector/issues/1)


# Links
