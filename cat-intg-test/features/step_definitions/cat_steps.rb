# encoding: UTF-8

require 'rspec'
require 'capybara/cucumber'
Capybara.default_driver= :selenium

When(/^I visist the cat "(.*?)"$/) do |target_url|
  visit target_url
end

Then(/^I can see the "(.*?)" of this page$/) do |content|
  page.has_content? content
end

