Urkund Content Review Service
===============================

Overview
--------
The Urkund Content Review service is an implementation of the
Content Review API's for Sakai. It is mainly based on the 
Turnitin Content Review service built by University of Cape Town.

Installation
------------
This module can be installed with the standard Sakai maven idiom.
Additionally, it can be included with a Sakai distribution as a top
level module as is typical with other 3rd party modules and tools.

Adjust the parent master version to match your 10.x installation.

The standard build command is the same:
mvn clean install sakai:deploy

Patching
--------
It is necessary to make some changes of the Assignment tool and the content-review module
and a patch for 10.6 is provided in the file sakai-10.6.patch.

The patch contains both source code and some english and swedish translations.

Post Install Configuration
--------------------------
After compiling and installing the code you need to add some
sakai.properties with your account information, and then set up
some quartz jobs that submit and fetch papers from the Urkund
web service. These are named "Process Content Review Queue" and 
"Process Content Review Reports".

Sakai Properties
----------------
urkund.service.username=<user id provided by Urkund>
urkund.service.password=<password provided by Urkund>

# Something like https://secure.urkund.com/api/rest/
urkund.service.url=<url for urkund web service>

# The email address must be an account in Urkund, like xxx@analysis.urkund.com
urkund.service.receiver=<the general email address to get all the submitted documents>

# Example value: course,project
urkund.sitetypes=<site types to enable for Urkund>

# The language to use for error messages from urkund
# Example value: sv-SE
# Default value is en-US
urkund.lang=<language code>

# If this property is set, the submitter email address is spoofed, like <id>.<str>@submitters.urkund.com
# This is used to prevent self plagiarism between different sites with same user
# Example value, which is used as the <str> in the template above: edu
# Default value is empty, meaning the submitter email address will be taken
# from the user submitting the document
urkund.spoofemailcontext=<2-3 characters long integration context>

# The number of times to retry submitting/getting report before giving up
# Default value is 10
urkund.maxRetry=<number of retries>

# Settings used by the Assignment tool
# All of these must be used as they hide settings not relevant to the Urkund service
assignment.useContentReview=true
turnitin.report.speed.setting.hide=true
turnitin.repository.setting.hide=true

turnitin.option.exclude_bibliographic=false
turnitin.option.exclude_quoted=false
turnitin.option.exclude_smallmatches=false
turnitin.option.s_paper_check=false
turnitin.option.internet_check=false
turnitin.option.journal_check=false

Logging
-------
#Enable more logging by setting debug
log4j.logger.org.sakaiproject.contentreview=DEBUG

Quartz (Cron) Jobs
------------------
There are 2 mandatory quartz jobs that need to be set up.

- | Content Review Queue 
  | Typical Cron Expression: 0 0/5 * * * ?
- | Process Content Review Reports
  | Typical Cron Expression: 0 0/5 * * * ?
  
Enabling Urkund on Specific Sites
---------------------------------
Out of the box, Urkund integration is enabled on all sites in the Sakai
instance. It is possible to restrict this by:

1) Using the urkund.sitetypes in the sakai properties

If the sitetypes property is restricting site types it is possible to enable Urkund on a specific site by:

2) Adding the site property "urkund", with value true

To make a more complex decision about which site to enable Urkund for one must
add a ContentReviewSiteAdvisor to the UrkundReviewServiceImpl and use that advisor
in the isSiteAcceptable().

Licensing
---------
Sakai and the Urkund Content Review Module is licensed under the [Educational Community License version 2.0](http://opensource.org/licenses/ECL-2.0)
