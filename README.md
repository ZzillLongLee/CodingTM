# CodingTM: Development Task Visualization for SW Code Comprehension

This is a development task visualization tool for SW Code Comprehension.
The development task is the coding activity that implements new feature, fixing bugs and refactoring, it is formed by code changes(task element) in the level of method and attribute and statement. This figure shows how development task is formed

<p align="center"><img src="https://user-images.githubusercontent.com/24516888/122871898-a26d5380-d36a-11eb-829a-e0ac4cdb9fd3.png" width="60%">

In this figure, method 1 is added and methods 2 and 3 are modified in the diff, also methods 2 and 3 use the method 1 in their own statement. So, we connect this relationship as a causal relationship, which can explain the reason of changed code like the dotted blue line. Based on these causal relationships, we formed the development task by grouping causal relationships. If you want to know identifying development task and its detail, please check the research paper we published at VISSOFT2021.

CodingTM visualize these development task in each of commits in the commit history like the time machine.

We implemented CodingTM as an Eclipse RCP application and made them accessible to the public.
This following figure shows the architecture of the CodingTM:

![The arcitecture of CodingTM](https://user-images.githubusercontent.com/24516888/122871894-a13c2680-d36a-11eb-9543-ef5342271c02.PNG)

As you can see, this CodingTM composed of 4 sequential views for a developer to comprehend source code.

### 1. Commit History View
Commit History View allows a developer to fetch a list of commits from the Github code repository in chronicle order and search a specific commit from the list of commits. This following figure shows the view with a comment for the general sequence of use.

<p align="center"><img src="https://user-images.githubusercontent.com/24516888/122871895-a1d4bd00-d36a-11eb-9de5-dc6ab13073d6.png" width="60%">

First, it starts with cloning a project from the code repository (see 1 in the figure) and showing all commits in the commit history table.
Second, developer can search specific commits depending on committer, commit message and commit id (see 2 ), which is useful to search a specific developer’s tasks across multiple commits.
Third, a developer can select commits through the checkbox and requests the system to see the tasks of each commit in chronological order through Development Task List View (see 3 ).

### 2. Development Task List View

Development Task List View plays a role in showing development tasks of the selected commits in chronological order, and it also shows the task elements and their change type for each task. The following figure below presents the development task list view.

<p align="center"><img src="https://user-images.githubusercontent.com/24516888/122871897-a1d4bd00-d36a-11eb-85bf-a0878736fffb.png" width="60%">

First, class name and task element columns shows identified task elements(method and attribute) in selected commits.
Second, two set of columns grouped with commit id shows the task element's change type and specific task.
Third, user can check identified development task by selecting the task in the specific task element(select changetype and right-click for popup menu and click the item in popup menu) like 1 in the figure. And also user can check how task element is changed and connected with other task element by selcting a change type of specific task element(select changetype and right-click for popup menu and click the item in popup menu) like 2 in the figure.

### 3. Development Task List View
Causal Relationship View shows identified development tasks by going into a specific point of commit.
This figure presents Causal Relationship View, showing tasks in an outer rounded rectangle and task elements in each task. The edge between task elements indicates a causal
relationship. The change type of each element is expressed in three different colors, where the green, yellow and red colors.

<p align="center"><img src="https://user-images.githubusercontent.com/24516888/122871901-a26d5380-d36a-11eb-8e69-e2e750153552.png" width="60%">

When selecting a specific task element as expressed in 1 , CodingTM shows the causal relationship table that helps one to explicitly navigate the causal relationships. Also, when a developer selects one of the task elements in the table, it shows Task Element Diff View for understanding the specific modified part of the task elements.

### 4. Task Element Diff View
Task Element Diff View shows which parts of a task element are updated in the statement level of detail and which task elements have causal relationships with the task
element as shown the figure below.

<p align="center"><img src="https://user-images.githubusercontent.com/24516888/122871892-a13c2680-d36a-11eb-832f-aac7bfd9e54b.png" width="60%">

This view is interactive with a user. When a developer selects a specific statement as designated at the 1 in the figure, this view highlights the updated code snippets in the Code View for intuitively supporting source code comprehension. Also, the selection makes the view updated to show other task elements that have causal relationships with the selected task elements.


### Prerequisites

1. install eclipse
2. install JavaSE-1.8
3. install nattable (help-install new software - (http://download.eclipse.org/nattable/releases/1.6.0/repository/)

## Getting Started
1. import codingTM as General -- existing projects into workspace.
2. run project.
