 ====================Setup jenkins:
 1. add to settings.xml in .m2 folder
  - Copy content from settins.jenkins.xml to ~/m2/settings.xml
  - Update password for server section configured, follow the link:
  http://maven.apache.org/guides/mini/guide-encryption.html

 2. Update view26 database setting:
 	- set transaction-isolation=READ-COMMITTEDdatabase
	
 3. release:
  - login to jenkins account page: https://accounts.jenkins.io, then add ssh key.
 	- mvn -Dresume=false release:prepare release:perform
	
 3.1. rollback a release:
  - mvn release:rollback
 
 3.2. clean a release:
  - mvn release:clean
 
 4. copy to local
  - cp target\view26.hpi \\192.168.74.80\Jenkins\view26.hpi
 
- https://docs-view26.atlassian.net/wiki/display/VIEW/VIEW26+Jenkins+Plugin