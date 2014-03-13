Feature: Login Salesforce
  In order to save time to do valuable work
  As a SalesForce developer
  I want to automate Salesforce deployment

  Scenario Outline: Browse the cat website
    When I visist the cat <URL>
    Then I can see the <Content> of this page

    Examples:
     |Type | URL  | Content |
     |"index"| "http://cat.qa.dianpingoa.com/cat/r" | "客户端版本说明" |
     |"event"| "http://cat.qa.dianpingoa.com/cat/r/e?domain=Cat&ip=All&date=2013101616&reportType=&op=view" | "2013-10-16 16:00:00 to 2013-10-16 16:59:59"|
