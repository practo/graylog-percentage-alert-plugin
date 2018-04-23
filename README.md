Overview
------------

Graylog alert plugin that allow you to trigger an alarm when given api failure percentage limit was breached.

Installation
------------

* Clone this repository.
* Run `mvn package` to build a JAR file.
* place the `.jar` file in your Graylog plugin directory. The plugin directory
  is the `plugins/` folder relative from your `graylog-server`
* Restart `graylog-server` and you are done.
