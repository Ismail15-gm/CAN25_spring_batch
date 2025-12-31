package com.can25.Batch;

import com.can25.Dto.SpectatorDTO;
import com.can25.Entity.SeatLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.batch.item.*;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.core.io.Resource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.HashMap;
import java.util.Map;

public class MainItemReader implements ItemReader<SpectatorDTO>, ItemStream {

    private final Resource resource;
    private ItemReader<SpectatorDTO> delegate;

    public MainItemReader(Resource resource) {
        this.resource = resource;
    }

    private void initializeDelegate() {
        if (delegate != null) {
            return;
        }

        String filename = resource.getFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Resource filename cannot be null");
        }

        if (filename.toLowerCase().endsWith(".json")) {
            this.delegate = createJsonReader();
        } else if (filename.toLowerCase().endsWith(".xml")) {
            this.delegate = createXmlReader();
        } else {
            throw new IllegalArgumentException("Unsupported file extension: " + filename);
        }
    }

    private ItemReader<SpectatorDTO> createJsonReader() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JacksonJsonObjectReader<SpectatorDTO> jsonObjectReader = new JacksonJsonObjectReader<>(SpectatorDTO.class);
        jsonObjectReader.setMapper(objectMapper);

        return new JsonItemReaderBuilder<SpectatorDTO>()
                .name("jsonSpectatorReader")
                .jsonObjectReader(jsonObjectReader)
                .resource(resource)
                .strict(false)
                .build();
    }

    private ItemReader<SpectatorDTO> createXmlReader() {
        Map<String, Class<?>> aliases = new HashMap<>();
        aliases.put("spectatorEntry", SpectatorDTO.class);
        aliases.put("seatLocation", SeatLocation.class);

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);
        marshaller.getXStream().allowTypes(new Class[] { SpectatorDTO.class, SeatLocation.class });

        return new StaxEventItemReaderBuilder<SpectatorDTO>()
                .name("xmlSpectatorReader")
                .resource(resource)
                .addFragmentRootElements("spectatorEntry")
                .unmarshaller(marshaller)
                .build();
    }

    @Override
    public SpectatorDTO read()
            throws Exception {
        if (delegate == null) {
            initializeDelegate();
        }
        return delegate.read();
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (delegate == null) {
            initializeDelegate();
        }
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).open(executionContext);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // delegate should be initialized by open()
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).update(executionContext);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).close();
        }
    }
}
