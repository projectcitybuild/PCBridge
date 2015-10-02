# PCBridge
A Bukkit plugin to bridge (www.projectcitybuild.com) and its game servers

### Current features:
* Ban system

### Want to contribute?
Great! But you'll first need to set up a local MySQL database named "PCBridge". You'll also need to create a table with the following SQL:

```
CREATE TABLE IF NOT EXISTS `pcban_active_bans` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `banned_uuid` varchar(60) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `banned_name` varchar(50) NOT NULL,
  `date_ban` int(8) NOT NULL,
  `date_expire` int(8) DEFAULT NULL,
  `staff_uuid` varchar(60) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `staff_name` varchar(50) NOT NULL,
  `reason` text NOT NULL,
  `ip` varchar(15) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=20 ;
```

Don't forget to also run the plugin once to generate the config file (plugin.yml). Add in your MySQL connection details as necessary.

### Todo List:
* Refactor MySQL Adapter to run asynchronous
* Temp banning
* Ban stats
* UUID lookup via Mojang web service (asynchronous)
