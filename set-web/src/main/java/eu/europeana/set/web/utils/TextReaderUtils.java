package eu.europeana.set.web.utils;

import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.model.bestbets.BestBetsUserSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TextReaderUtils {

    protected static final Logger LOG = LogManager.getLogger(TextReaderUtils.class);

    public static void readTxtFile(String directoryLocation, List<BestBetsUserSet> bestBetsUerSets) {
    try (BufferedReader br = new BufferedReader(new FileReader(new File(directoryLocation + WebUserSetFields.SLASH + WebUserSetFields.BEST_BETS_FILENAME)))) {
        String line;
        List<String> items = new ArrayList<>();
        String entityId = null;
        int totalEntities =0;
        while ((line = br.readLine()) != null) {
            if(!line.isEmpty()) {
                if(line.startsWith(WebUserSetFields.ENTITY_HEADER)) {
                    entityId = StringUtils.substringAfter(line,WebUserSetFields.ENTITY_HEADER).trim();
                    totalEntities ++ ;
                } else {
                    items.add(line.trim());
                }
            } else {
                bestBetsUerSets.add(new BestBetsUserSet(entityId, items));
                // initialise again
                items = new ArrayList<>();
                entityId = null;
            }
        }
        // fall back check
        if(totalEntities != bestBetsUerSets.size()) {
            throw new IOException("Error reading Best Bets from the file " +directoryLocation + "/" +WebUserSetFields.BEST_BETS_FILENAME);
        }
        } catch (FileNotFoundException e) {
            LOG.error("Best Bets file {} not found at {}", WebUserSetFields.BEST_BETS_FILENAME, directoryLocation, e);
        } catch (IOException e) {
            LOG.error("Error reading the file. {}", e.getMessage());
        }
    }
}
