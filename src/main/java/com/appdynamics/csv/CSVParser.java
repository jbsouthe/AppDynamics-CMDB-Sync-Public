package com.appdynamics.csv;

import com.appdynamics.cmdb.EntityType;
import com.appdynamics.controller.ControllerService;
import com.appdynamics.exceptions.ControllerBadStatusException;
import com.appdynamics.controller.apidata.cmdb.BatchTaggingRequest;
import com.appdynamics.exceptions.ParserException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVParser {
    private List<String> header;
    private List<List<String>> records;

    public CSVParser (String csvFileName) throws ParserException {
        try {
            records = parse(csvFileName);
        } catch (IOException e) {
            throw new ParserException(e.getMessage());
        }
        if( records == null || records.size() < 2 ) {
            throw new ParserException("No Records Found, nothing to insert");
        }
        header = records.remove(0);
        if( !"EntityType".equalsIgnoreCase(header.get(0)) ) throw new ParserException("EntityType must be the first header column");
        if( !"Name".equalsIgnoreCase(header.get(1)) ) throw new ParserException("Name must be the second header column");
        if( !"Application".equalsIgnoreCase(header.get(2)) ) throw new ParserException("Application must be the third header column");
        if( header.size() < 4 ) throw new ParserException("No Tags Defined, only required columns?");
    }

    private enum State {
        UNQUOTED_FIELD,
        QUOTED_FIELD,
        QUOTE_IN_QUOTED_FIELD
    }

    public List<List<String>> parse(String fileName) throws IOException {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<String> record = new ArrayList<>();
                StringBuilder field = new StringBuilder();
                State state = State.UNQUOTED_FIELD;

                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    switch (state) {
                        case UNQUOTED_FIELD:
                            switch (c) {
                                case ',':
                                    record.add(field.toString());
                                    field.setLength(0);
                                    break;
                                case '"':
                                    state = State.QUOTED_FIELD;
                                    break;
                                default:
                                    field.append(c);
                                    break;
                            }
                            break;

                        case QUOTED_FIELD:
                            switch (c) {
                                case '"':
                                    state = State.QUOTE_IN_QUOTED_FIELD;
                                    break;
                                default:
                                    field.append(c);
                                    break;
                            }
                            break;

                        case QUOTE_IN_QUOTED_FIELD:
                            switch (c) {
                                case '"':
                                    field.append('"');
                                    state = State.QUOTED_FIELD;
                                    break;
                                case ',':
                                    record.add(field.toString());
                                    field.setLength(0);
                                    state = State.UNQUOTED_FIELD;
                                    break;
                                default:
                                    throw new IllegalArgumentException("Invalid CSV format");
                            }
                            break;
                    }
                }
                // Add the last field
                record.add(field.toString());
                records.add(record);
            }
        }
        return records;
    }

    public Collection<BatchTaggingRequest> getBatchRequests(ControllerService controller) throws ParserException {
        Map<EntityType,BatchTaggingRequest> requests = new HashMap<>();
        for( List<String> record : this.records ) {
            EntityType type = EntityType.valueOfIgnoreCase(getEntityType(record));
            BatchTaggingRequest request = requests.get(type);
            if( request == null ) {
                request = new BatchTaggingRequest(type);
                requests.put(type,request);
            }
            request.addEntity(getEntityName(record), getEntityId(controller, type, record), getTags(record));
        }

        return requests.values();
    }

    private Map<String,String> getTags (List<String> record) {
        Map<String,String> map = new HashMap<>();
        for( int i = 3; i< record.size(); i++) {
            map.put(header.get(i), record.get(i));
        }
        return map;
    }

    private Long getEntityId (ControllerService controller, EntityType type, List<String> record) throws ParserException {
        String name = getEntityName(record);
        switch (type) {
            case Server -> {
                try {
                    return controller.getServerId( name );
                } catch (ControllerBadStatusException e) {
                    throw new ParserException(e.getMessage());
                }
            }
            case Application -> {
                return controller.getApplicationId( name );
            }
            case Tier -> {
                return controller.getModel().getApplication(getEntityApplication(record)).getTier(name).id;
            }
            case Node -> {
                return controller.getModel().getApplication(getEntityApplication(record)).getNode(name).id;
            }
            case BusinessTransaction -> {
                return controller.getModel().getApplication(getEntityApplication(record)).getBusinessTransaction(name).id;
            }
            /*case SyntheticPage -> {
                return controller.getModel().getSyntheticApplication(getEntityApplication(record)).getPage(name).id;
            }*/
            default -> {
                throw new ParserException(String.format("EntityType %s is not yet supported",type));
            }
        }
    }

    private String getEntityApplication (List<String> record) {
        return record.get(2);
    }

    private String getEntityName (List<String> record) {
        return record.get(1);
    }

    private String getEntityType (List<String> record) {
        return record.get(0);
    }
}
