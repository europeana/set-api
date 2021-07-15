package eu.europeana.set.web.utils;

import eu.europeana.api.common.config.UserSetI18nConstants;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.web.exception.request.BestBetsMismatchException;
import eu.europeana.set.web.model.bestbets.BestBetsUserSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TextReaderUtils {

    protected static final Logger LOG = LogManager.getLogger(TextReaderUtils.class);

    public void readTxtFile(List<BestBetsUserSet> bestBetsUerSets) throws BestBetsMismatchException, IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(getFile(WebUserSetFields.BEST_BETS_FILE)))) {
        String line;
        List<String> items = new ArrayList<>();
        String entityId = null;
        int totalEntities = 0;
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                if (line.startsWith(WebUserSetFields.ENTITY_HEADER)) {
                    entityId = StringUtils.substringAfter(line, WebUserSetFields.ENTITY_HEADER).trim();
                    totalEntities++;
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
        if (totalEntities != bestBetsUerSets.size()) {
            throw new BestBetsMismatchException(UserSetI18nConstants.BEST_BETS_LOAD_ERROR,
                    new String[]{ String.valueOf(totalEntities), String.valueOf(bestBetsUerSets.size())});
        }
    }
    }

    //TODO decide the best way to load the file
    /**
     * loads the best bets file from classpath
     *
     * @return
     */
    private File getFile(String fileName) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(fileName);
    if (resource == null) {
        throw new IOException(WebUserSetFields.BEST_BETS_FILE + "file not found!");
    } else {
        return new File(resource.getFile());
    }
    }
}
