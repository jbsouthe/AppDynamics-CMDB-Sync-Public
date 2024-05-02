package com.appdynamics;

import com.appdynamics.cmdb.EntityType;
import com.appdynamics.controller.ControllerService;
import com.appdynamics.csv.CSVFile;
import com.appdynamics.csv.CSVParser;
import com.appdynamics.controller.apidata.cmdb.BatchResponse;
import com.appdynamics.controller.apidata.cmdb.BatchTaggingRequest;
import com.appdynamics.controller.apidata.model.*;
import com.appdynamics.controller.apidata.server.Server;
import com.appdynamics.cryptography.AES256Cryptography;
import com.appdynamics.exceptions.CMDBBadStatusException;
import com.appdynamics.exceptions.ControllerBadStatusException;
import com.appdynamics.exceptions.InvalidConfigurationException;
import com.appdynamics.exceptions.ParserException;
import com.appdynamics.config.Configuration;
import com.appdynamics.scheduler.MainControlScheduler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMDBSyncMain {
    private static final Logger logger = LogManager.getFormatterLogger();

    public static List<String> getCommands( Namespace namespace ) {
        List<String> commands = new ArrayList<>();
        try {
            commands = namespace.getList("command");
        } catch (java.lang.ClassCastException ignored) {
            commands.add(namespace.getString("command"));
        }
        return commands;
    }

    private static void printHelpAndExit( ArgumentParser parser, int exitCode ) {
        parser.printHelp();
        System.exit(exitCode);
    }

    private static void configureLogging(Level level) {
        ConfigurationBuilder<BuiltConfiguration> builder = org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory.newConfigurationBuilder();

        builder.setStatusLevel(Level.WARN);
        builder.setMonitorInterval("30");

        // Add layout
        LayoutComponentBuilder standardLayout = builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n");

        // Add console appender with layout
        AppenderComponentBuilder consoleAppender = builder.newAppender("Console", "Console")
                .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
                .add(standardLayout);
        builder.add(consoleAppender);

        // METRIC_LOGGER_File Appender
        AppenderComponentBuilder missingDataAppender = builder.newAppender("METRIC_LOGGER_File", "File")
                .addAttribute("fileName", "logs/unmatchedQueries-${date:yyyy-MM-dd-HHmmss}.log")
                .add(standardLayout);
        builder.add(missingDataAppender);

        // File Appender
        AppenderComponentBuilder fileAppender = builder.newAppender("File", "File")
                .addAttribute("fileName", "logs/sync-${date:yyyy-MM-dd-HHmmss}.log")
                .add(standardLayout);
        builder.add(fileAppender);

        // Logger Definitions
        builder.add(builder.newLogger("QUERY_LOGGER", Level.INFO)
                .add(builder.newAppenderRef("METRIC_LOGGER_File"))
                .addAttribute("additivity", false));

        // Root Logger
        builder.add(builder.newRootLogger(level)
                .add(builder.newAppenderRef("File"))
                .add(builder.newAppenderRef("Console")));

        // Initialize Log4j with the configuration
        BuiltConfiguration config = builder.build();
        Configurator.initialize(config);
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        ctx.setConfiguration(config);
    }

    public static void main( String... args ) {

        ArgumentParser parser = ArgumentParsers.newFor("CMDBSync")
                .singleMetavar(true)
                .build()
                .defaultHelp(true)
                .version(String.format("ServiceNow CMDB Sync Tool version %s build date %s written by %s", MetaData.VERSION, MetaData.BUILDTIMESTAMP, MetaData.CONTACT_GECOS))
                .description("Manage ServiceNow CMDB Sync Data into an AppDynamics ControllerService");
        parser.addArgument("-v", "--version").action(Arguments.version());
        parser.addArgument("-c", "--config")
                .setDefault("default-config.xml")
                .metavar("./config-file.xml")
                .help("Use this specific XML config file.");
        parser.addArgument("-k", "--keyfile")
                .metavar("./keyfile.key")
                .help("Use this key for encryption");
        parser.addArgument("-l", "--level")
                .setDefault("INFO")
                .metavar("INFO")
                .help("Set logging level {TRACE|DEBUG|INFO|WARN|ERROR}");
        parser.addArgument("command")
                .nargs("*")
                .help("Commands are probably too flexible, some examples include: {\"genkey [key file name]\", \"encrypt [string]\", \"query [table] [string]\", \"<get | delete> <EntityType> [Application] [Name Name ...]\", \"insert <csv file>\", \"export <csv file>\", \"executeScheduler\" }")
                .setDefault("executeScheduler");

        Namespace namespace = null;
        try {
            namespace = parser.parseArgs(args);
            logger.info("parser: %s", namespace);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        List<String> commands = getCommands(namespace);
        String configFileName = namespace.getString("config");
        String keyFileName = namespace.getString("keyfile");
        String logLevel = namespace.getString("level");

        configureLogging(Level.getLevel(logLevel));


        logger.info("Initializing CMDB Sync Tool version %s build date %s, please report any problems directly to %s", MetaData.VERSION, MetaData.BUILDTIMESTAMP, MetaData.CONTACT_GECOS);


        boolean forceExit=true;
        switch (commands.get(0).toLowerCase()) {
            case "get": {
                List<Object> parts = namespace.getList("command");
                if(parts.size() < 2) throw new IllegalArgumentException("Not enough arguments for this command");
                String type = parts.get(1).toString();
                String appOrName = parts.get(2).toString();
                List<String> otherNames = new ArrayList<>();
                for( int i=3; i< parts.size(); i++ ) {
                    otherNames.add( parts.get(i).toString());
                }
                Configuration config = null;
                try {
                    config = new Configuration(configFileName, "controller", keyFileName);
                } catch (IOException e) {
                    logger.fatal("Can not read configuration file: %s Exception: %s", configFileName, e.getMessage());
                    return;
                } catch (SAXException e) {
                    logger.fatal("XML Parser Error reading config file: %s Exception: %s", configFileName, e.getMessage());
                    return;
                } catch (Exception e) {
                    logger.fatal("A configuration exception was thrown that we can't handle, so we are quiting, Exception: ",e);
                    return;
                }
                String response = config.getController().getTags(type, appOrName, otherNames.toArray(new String[0]));
                System.out.println("Response: "+response);
                break;
            }
            case "delete": {
                List<Object> parts = namespace.getList("command");
                if(parts.size() < 2) throw new IllegalArgumentException("Not enough arguments for this command");
                String type = parts.get(1).toString();
                String appOrName = parts.get(2).toString();
                List<String> otherNames = new ArrayList<>();
                for( int i=3; i< parts.size(); i++ ) {
                    otherNames.add( parts.get(i).toString());
                }
                Configuration config = null;
                try {
                    config = new Configuration(configFileName, "controller", keyFileName);
                } catch (IOException e) {
                    logger.fatal("Can not read configuration file: %s Exception: %s", configFileName, e.getMessage());
                    return;
                } catch (SAXException e) {
                    logger.fatal("XML Parser Error reading config file: %s Exception: %s", configFileName, e.getMessage());
                    return;
                } catch (Exception e) {
                    logger.fatal("A configuration exception was thrown that we can't handle, so we are quiting, Exception: ",e);
                    return;
                }
                String response = config.getController().deleteTags(type, appOrName, otherNames.toArray(new String[0]));
                System.out.println("Response: "+response);
                break;
            }
            case "query": {
                StringBuilder query = new StringBuilder();
                List<Object> parts = namespace.getList("command");
                String tableURL = parts.get(1).toString();
                for( int i=2; i< parts.size(); i++ ) {
                    query.append(parts.get(i).toString()).append(" ");
                }
                Configuration config = null;
                try {
                    config = new Configuration(configFileName, "cmdb", keyFileName);
                } catch (IOException e) {
                    logger.fatal("Can not read configuration file: %s Exception: %s", configFileName, e.getMessage());
                    return;
                } catch (SAXException e) {
                    logger.fatal("XML Parser Error reading config file: %s Exception: %s", configFileName, e.getMessage());
                    return;
                } catch (Exception e) {
                    logger.fatal("A configuration exception was thrown that we can't handle, so we are quiting, Exception: ",e);
                    return;
                }
                Map<String,String> parameters = new HashMap<>();
                parameters.put("sysparm_query", query.toString() );
                //parameters.put("sysparm_fields", "");
                parameters.put("sysparm_display_value","true");
                parameters.put("sysparm_exclude_reference_link","true");
                String json = null;
                try {
                    System.out.println("Running Query: "+ query.toString());
                    json = config.getCmdbClient().getRequest(tableURL, parameters);
                    System.out.println(json);
                } catch (CMDBBadStatusException e) {
                    System.err.println("Error attempting to get CMDB data: "+ e.getMessage());
                }

                break;
            }
            case "executescheduler": {
                Configuration config = null;
                try {
                    config = new Configuration(configFileName, null, keyFileName);
                } catch (IOException e) {
                    logger.fatal("Can not read configuration file: %s Exception: %s", configFileName, e.getMessage());
                    return;
                } catch (SAXException e) {
                    logger.fatal("XML Parser Error reading config file: %s Exception: %s", configFileName, e.getMessage());
                    return;
                } catch (Exception e) {
                    logger.fatal("A configuration exception was thrown that we can't handle, so we are quiting, Exception: ",e);
                    return;
                }
                forceExit=false;
                MainControlScheduler mainControlScheduler = new MainControlScheduler( config );
                mainControlScheduler.run();
                break;
            }
            case "encrypt": {
                Configuration config = new Configuration();
                try {
                    config.setEncryption(keyFileName);
                } catch (InvalidConfigurationException e) {
                    logger.fatal("Invalid Encryption Key, Exception: %s", e);
                    return;
                }
                StringBuilder stringToEncrypt = new StringBuilder();
                List<Object> parts = namespace.getList("command");
                for( int i=1; i< parts.size(); i++ ) {
                    stringToEncrypt.append(parts.get(i).toString()).append(" ");
                }
                try {
                    System.out.println("Encrypted String: " + config.encrypt(stringToEncrypt.toString()));
                } catch (Exception e) {
                    System.err.println("Error encrypting the string, message: "+ e);
                    printHelpAndExit(parser, 1);
                }
                break;
            }
            case "genkey": {
                List<Object> parts = namespace.getList("command");
                String genKeyFileName = parts.get(1).toString();
                AES256Cryptography.generateKey(genKeyFileName);
                File keyFile = new File(genKeyFileName);
                if(keyFile.exists()) {
                    System.out.println(String.format("Key Generated: %s", keyFile.getAbsolutePath()));
                } else {
                    System.out.println("Key file not generated");
                }
                break;
            }
            case "insert": {
                List<Object> parts = namespace.getList("command");
                String csvFileName = parts.get(1).toString();
                Configuration config = null;
                try {
                    config = new Configuration(configFileName, "controller", keyFileName);
                } catch (IOException e) {
                    logger.fatal("Can not read configuration file: %s Exception: %s", configFileName, e.getMessage());
                    return;
                } catch (SAXException e) {
                    logger.fatal("XML Parser Error reading config file: %s Exception: %s", configFileName, e.getMessage());
                    return;
                } catch (Exception e) {
                    logger.fatal("A configuration exception was thrown that we can't handle, so we are quiting, Exception: ",e);
                    return;
                }
                GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
                Gson gson = builder.create();
                try {
                    CSVParser csvParser = new CSVParser(csvFileName);
                    for( BatchTaggingRequest batchTaggingRequest : csvParser.getBatchRequests(config.getController()) ){
                        BatchResponse response = config.getController().updateTags(batchTaggingRequest);
                        System.out.println(String.format("Request: %s Response: %s",gson.toJson(batchTaggingRequest), gson.toJson(response.toString())));
                    }
                } catch (ParserException e) {
                    throw new RuntimeException(e);
                } catch (ControllerBadStatusException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case "export": {
                List<Object> parts = namespace.getList("command");
                String csvFileName = parts.get(1).toString();
                Configuration config = null;
                try {
                    config = new Configuration(configFileName, "controller", keyFileName);
                } catch (IOException e) {
                    logger.fatal("Can not read configuration file: %s Exception: %s", configFileName, e.getMessage());
                    return;
                } catch (SAXException e) {
                    logger.fatal("XML Parser Error reading config file: %s Exception: %s", configFileName, e.getMessage());
                    return;
                } catch (Exception e) {
                    logger.fatal("A configuration exception was thrown that we can't handle, so we are quiting, Exception: ",e);
                    return;
                }
                GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
                Gson gson = builder.create();
                try {
                    CSVFile csvFile = new CSVFile(csvFileName);
                    ControllerService controller = config.getController();
                    Model model = controller.getModel();
                    for(Server server : controller.getServerList()) {
                        csvFile.add(controller.getEntityTags(server.getId(), EntityType.Server), EntityType.Server);
                    }
                    for(Application application : controller.applications) {
                        csvFile.add(controller.getEntityTags(application.getId(), EntityType.Application), EntityType.Application);
                        for(Tier tier: application.tiers) {
                            csvFile.add(application.name, controller.getEntityTags(tier.getId(), EntityType.Tier), EntityType.Tier);
                        }
                        for(Node node: application.nodes) {
                            csvFile.add(application.name, controller.getEntityTags(node.getId(), EntityType.Node), EntityType.Node);
                        }
                        for(BusinessTransaction businessTransaction: application.businessTransactions) {
                            csvFile.add(application.name, controller.getEntityTags(businessTransaction.getId(), EntityType.BusinessTransaction), EntityType.BusinessTransaction);
                        }
                    }
                    csvFile.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            default: printHelpAndExit(parser, -1);
        }
        if( forceExit ) System.exit(0);

    }
}
