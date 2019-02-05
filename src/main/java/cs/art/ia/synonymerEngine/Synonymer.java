package cs.art.ia.synonymerEngine;


import java.util.*;


import cs.art.ia.utils.Utility;
import cs.art.ia.utils.Pairs;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;


public class Synonymer {


    private WordNetDatabase database;

    public Synonymer() {
        database = WordNetDatabase.getFileInstance();
    }


    /**
     * Cerca la lista dei vari nounSynsets per la parola data
     * @param word
     * @return lista di nounSynset della parola data.
     */
    private List<NounSynset> getSynsetsFromWord(String word) {

        Synset[] synsets = database.getSynsets(word, SynsetType.NOUN);
        List<NounSynset> listNounSynset = new ArrayList<NounSynset>();
        for (Synset synset : synsets) {
            if (synset instanceof NounSynset) {
                listNounSynset.add((NounSynset) synset);
            }
        }
        return listNounSynset;

    }

    /**
     * Cerca la lista di parole all'interno dei vari nounSynsets
     * @param nounSynsets
     * @return lista di parole all'interno dei vari synset
     */
    private List<String> getWordsSynsets(List<NounSynset> nounSynsets) {
        List<String> listSynonyms = new ArrayList<String>();
        for (NounSynset nounSynset : nounSynsets) {
            for (String word : nounSynset.getWordForms()) {
                listSynonyms.add(word);
            }
        }
        return listSynonyms;
    }


    /**
     * Data una query, trova i sinonimi per gli elementi contenuti all'interno della query
     * @return lista di query contenente i sinonimi trovati.
     */
    public List<String> fetchSynonyms(String resource) {
        {

            List<String> synonymQuery;

            List<NounSynset> synsets = getSynsetsFromWord(resource);
            List<String> synonyms = getWordsSynsets(synsets);
            Set<String> foo = new HashSet<String>(synonyms);
            synonymQuery = new ArrayList<>(foo);

            for (String obj : synonymQuery) {
                System.out.println(obj);

            }
            return synonymQuery;
        }
    }


    /**
     * @param depthLevel
     * @return List of hyponyms at a specified depth level.
     */

    public List<String> fetchHyponyms(String resource, Integer depthLevel) {

        List<String> hyponymsQuery = new ArrayList<String>();
        List<NounSynset> listSynsets = getSynsetsFromWord(resource);
        List<String> listHyponyms = new ArrayList<String>();
        for (int i = 1; i <= depthLevel; i++) {
            List<NounSynset> listSynsetHyponymsByLevel = new ArrayList<NounSynset>();

            for (NounSynset synset : listSynsets) {
                NounSynset[] hyponyms;
                hyponyms = synset.getHyponyms();
                listSynsetHyponymsByLevel.addAll(Arrays.asList(hyponyms));
            }
            listSynsets.clear();
            listSynsets.addAll(listSynsetHyponymsByLevel);
        }

        // Reached depth level k.
        listHyponyms.addAll(getWordsSynsets(listSynsets));
        //La puliamo da precedenti iteraziini a causa del riempimento di sinonimi precedente
        hyponymsQuery.clear();
        hyponymsQuery = (Utility.eliminateDuplicatePropertyQuery(listHyponyms));

        for (String obj : hyponymsQuery) {
            System.out.println(obj);
        }

        return hyponymsQuery;
    }

    /**
     * @return List of hypernyms at a specified height level.
     */

    public List<String> fetchHypernyms(String resource, Integer heightLevel) {

//		List<Pairs<String,List<String>>> hypernymsQuery= determinateResource(resource);

        List<String> hypernymsQuery = new ArrayList<String>();

        List<NounSynset> listSynsets = getSynsetsFromWord(resource);

        List<String> listHypernyms = new ArrayList<String>();

        for (int i = 1; i <= heightLevel; i++) {
            List<NounSynset> listSynsetHypernyms = new ArrayList<NounSynset>();

            for (NounSynset synset : listSynsets) {
                NounSynset[] hypernyms;
                hypernyms = synset.getHypernyms();
                listSynsetHypernyms.addAll(Arrays.asList(hypernyms));
            }

            // Remove previous level of synsets
            listSynsets.clear();
            // Update the list of synsets related to the current depth level.
            listSynsets.addAll(listSynsetHypernyms);
        }
        // Reached depth level k.
        listHypernyms.addAll(getWordsSynsets(listSynsets));
//			int i=hypernymsQuery.indexOf(obj);

        //La puliamo da precedenti iteraziini a causa del riempimento di sinonimi precedente
        hypernymsQuery.clear();
        hypernymsQuery = (Utility.eliminateDuplicatePropertyQuery(listHypernyms));


        for (String obj : hypernymsQuery) {
            System.out.println(obj);
//            printResourceAssociateVariant(obj);
        }

        return hypernymsQuery;
    }

}