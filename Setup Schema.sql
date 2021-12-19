use databasename;
/**
Query below is for creation of the User table.This should be run first because other tables
are dependent on this due to the foreign key
**/
CREATE TABLE `Users` (
  `UserID` int(11) NOT NULL AUTO_INCREMENT,
  `deviceHash` varchar(255) NOT NULL,
  `lastSync` datetime DEFAULT NULL,
  PRIMARY KEY (`UserID`),
  KEY `personHashIN` (`deviceHash`)
);

/**
Query below is for the creation of the covid_test_result table which stores the test results of people.
**/

CREATE TABLE `covid_test_results` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `UserID` int(11) DEFAULT NULL,
  `date` date DEFAULT NULL,
  `result` tinyint(4) DEFAULT NULL,
  `testHash` varchar(45) DEFAULT NULL,
  `createdTime` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `UserID` (`UserID`),
  CONSTRAINT `covid_test_results_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `Users` (`UserID`)
) ;

/**
Query below is for the creation of the contacted table which stores the meetings and the details of the meetingd between people.
**/

CREATE TABLE `Contacted` (
  `ContactedID` bigint(20) NOT NULL AUTO_INCREMENT,
  `sourceFkID` int(11) DEFAULT NULL,
  `contactedFkID` int(11) DEFAULT NULL,
  `contactDuration` int(11) DEFAULT NULL,
  `contactedDate` date DEFAULT NULL,
  `deleted` tinyint(1) unsigned zerofill DEFAULT 0,
  `createTime` datetime DEFAULT NULL,
  `createUser` varchar(255) DEFAULT NULL,
  `updateUser` varchar(255) DEFAULT NULL,
  `updateTime` datetime DEFAULT NULL,
  PRIMARY KEY (`ContactedID`),
  KEY `contactedFk` (`contactedFkID`),
  KEY `sourceIDIn` (`sourceFkID`,`contactedFkID`,`contactedDate`,`contactDuration`),
  CONSTRAINT `contactedFk` FOREIGN KEY (`contactedFkID`) REFERENCES `Users` (`UserID`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `sourceFk` FOREIGN KEY (`sourceFkID`) REFERENCES `Users` (`UserID`) ON DELETE NO ACTION ON UPDATE NO ACTION
);
