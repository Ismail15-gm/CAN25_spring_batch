package com.can25.Batch;

import com.can25.Dto.SpectatorDTO;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class MainItemReader implements ItemReader<SpectatorDTO>, ItemStream {

    private final ItemReader<SpectatorDTO> jsonReader;
    private final ItemReader<SpectatorDTO> xmlReader;
    private final String sourceType;

    public MainItemReader(ItemReader<SpectatorDTO> jsonReader, ItemReader<SpectatorDTO> xmlReader, String sourceType) {
        this.jsonReader = jsonReader;
        this.xmlReader = xmlReader;
        this.sourceType = sourceType;
    }

    @Override
    public SpectatorDTO read()
            throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if ("xml".equalsIgnoreCase(sourceType)) {
            return xmlReader.read();
        } else {
            return jsonReader.read();
        }
    }

    @Override
    public void open(org.springframework.batch.item.ExecutionContext executionContext) throws ItemStreamException {
        if ("xml".equalsIgnoreCase(sourceType)) {
            if (xmlReader instanceof ItemStream) {
                ((ItemStream) xmlReader).open(executionContext);
            }
        } else {
            if (jsonReader instanceof ItemStream) {
                ((ItemStream) jsonReader).open(executionContext);
            }
        }
    }

    @Override
    public void update(org.springframework.batch.item.ExecutionContext executionContext) throws ItemStreamException {
        if ("xml".equalsIgnoreCase(sourceType)) {
            if (xmlReader instanceof ItemStream) {
                ((ItemStream) xmlReader).update(executionContext);
            }
        } else {
            if (jsonReader instanceof ItemStream) {
                ((ItemStream) jsonReader).update(executionContext);
            }
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if ("xml".equalsIgnoreCase(sourceType)) {
            if (xmlReader instanceof ItemStream) {
                ((ItemStream) xmlReader).close();
            }
        } else {
            if (jsonReader instanceof ItemStream) {
                ((ItemStream) jsonReader).close();
            }
        }
    }
}
