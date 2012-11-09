Privileges
===
Privileges seeks to be a familiar face for Permissions, and uses the Permissions 2.x format of lists of nodes for users. It offers inheritance, multi-world permissions, and promotion tracks

by **krinsdeath**

Want to Contribute?
---
If you have an idea that you think Privileges needs, and I'm not responding (for whatever reason), feel free to submit a pull request!

Privileges uses Maven to manage its dependencies, and is dependent on the Java 5 JDK or later. I use 4 spaces for indentation. When committing changes to your local branch, try to keep each feature in its own commit.

    git clone https://github.com/krinsdeath/Privileges.git
    cd Privileges
    mvn -U clean install

Features
---
*   Multi-world compatible permissions management, with a familiar interface
*   Simple and straightforward inheritance system
*   Promotion and demotion commands
*   "Anti-build" with permissions nodes 'privileges.build' and 'privileges.interact' as well as permissions for specific blocks 'privileges.interact.[block id]'
*   Extremely powerful and easy-to-use command system!
