# User Login Specification

* On Login page

## Registered user login to the system
Use tags to enrich information about:
Which interface is used? user, admin, manager, ...
What functionality is tested?  signup, login, serach, ...
What is the status of the implementation? inprogress, final, dericated, ...
What is the execution priority / iteration? low, medium, high, smoke, nightly, ...
e.g.:

tags: registered-user, successful, login

* Login as "tfernando+056@mitrai.com" using "Pass@123"
* Logout from the application

* Configure ZAP Vulnerability Scan Settings and Start the Scanning 

   |attack_alert_threshold|attack_strength|passive_scan|
   |----------------------|---------------|------------|
   |MEDIUM                |MEDIUM         |true        |

* Exclude following URLs from the Scan 

   |url                        |
   |---------------------------|
   |https://accounts.google.com|


* Validate Vulnerability Alerts are less than 

   |low|medium|high|
   |---|------|----|
   |30 |10    |0   |


