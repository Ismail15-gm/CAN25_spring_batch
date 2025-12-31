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

/**
 * CUSTOM ITEM READER (The "Smart" Reader)
 * This class implements ItemReader and ItemStream.
 * It acts as a "Router" or "Delegate" pattern.
 *
 * Problem: Use one reader class to handle BOTH .json and .xml files.
 * Solution:
 * 1. Check the file extension at runtime.
 * 2. If .json, create and use a JacksonJsonObjectReader.
 * 3. If .xml, create and use a StaxEventItemReader.
 */
public class MainItemReader implements ItemReader<SpectatorDTO>, ItemStream {

    private final Resource resource;
    private ItemReader<SpectatorDTO> delegate; // The actual reader (JSON or XML) that does the work

    public MainItemReader(Resource resource) {
        this.resource = resource;
    }

    /**
     * Initializes the correct delegate reader based on file extension.
     * This logic runs lazily (only when needed).
     */
    private void initializeDelegate() {
        if (delegate != null) {
            return;
        }

        String filename = resource.getFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Resource filename cannot be null");
        }

        // Dynamic Switch
        if (filename.toLowerCase().endsWith(".json")) {
            this.delegate = createJsonReader();
        } else if (filename.toLowerCase().endsWith(".xml")) {
            this.delegate = createXmlReader();
        } else {
            throw new IllegalArgumentException("Unsupported file extension: " + filename);
        }
    }

    /**
     * Creates a JSON reader using Jackson.
     */
    private ItemReader<SpectatorDTO> createJsonReader() {
        // ObjectMapper is the library that converts JSON text -> Java Objects
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Support for Java 8 Dates (LocalDateTime)
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

    /**
     * Creates an XML reader using Stax (Streaming API for XML) and XStream.
     */
    private ItemReader<SpectatorDTO> createXmlReader() {
        // Aliases map XML tag names to Java Classes
        Map<String, Class<?>> aliases = new HashMap<>();
        aliases.put("spectatorEntry", SpectatorDTO.class); // <spectatorEntry> -> SpectatorDTO
        aliases.put("seatLocation", SeatLocation.class); // <seatLocation> -> SeatLocation

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);
        // Security configuration: explicitly allow our types
        marshaller.getXStream().allowTypes(new Class[] { SpectatorDTO.class, SeatLocation.class });

        return new StaxEventItemReaderBuilder<SpectatorDTO>()
                .name("xmlSpectatorReader")
                .resource(resource)
                .addFragmentRootElements("spectatorEntry") // The tag that represents one single item
                .unmarshaller(marshaller)
                .build();
    }

    /**
     * The main read method called by Spring Batch.
     * It simply delegates the call to the actual specific reader (JSON or XML).
     */
    @Override
    public SpectatorDTO read()
            throws Exception {
        if (delegate == null) {
            initializeDelegate();
        }
        return delegate.read();
    }

    // --- ItemStream Methods ---
    // These are required to handle opening/closing resources and state management

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
/**
 *
 * keep in mind :
 * -------------------
 * JsonItemReader → implements ItemReader and ItemStream
 * -----------------------------------------
 * open()   →  read/process/write (chunks)
 *             ↳ update() after each commit
 * close()  →  step ends
 *------------------------------------------
 *
 * The "Invisible" Reader: Spring Batch automatically manages the lifecycle (open/close) of
beans it can "see". However, in our proxy design, the real reader (JSON or XML) is hidden inside
MainItemReader

as a private field (delegate). Spring doesn't know it exists.
The Bridge: By implementing ItemStream on MainItemReader we create a "bridge". When Spring calls open()
on our wrapper class, we manually forward that call to the hidden inner reader.
Avoiding Crashes: If we didn't do this, the inner reader would never be "opened" (initialized),
and the job would crash with a ReaderNotOpenException as soon as it tried to read the first line.


 */