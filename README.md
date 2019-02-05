# QAMOVIE
### Progetto di Intelligenza Artificiale 2

## Descrizione breve:

Implementazione di un sistema di Question Answering orientato all'universo cinematografico, in grado di tradurre domande da linguaggio naturale in query SPARQL, sfruttando una ontologia dedicata.

L'applicazione è stata realizzata interamente in Java in 3 step logici:
- Template parser: permette l'elaborazione immediata di Wh- questions tramite l'analisi di alcuni template di esempio, operando un pattern matching per il riconoscimento (tramite espressioni regolari ad-hoc) di: soggetto, predicato ed oggetto relativo al predicato; 
- Synonymer: per la gestione dei fenomeni di sinonimia, iponimia ed iperonimia della domanda di input, sfruttando il noto database semantico-lessicale di WordNet;
- Parser sintattico: elabora diversamente la domanda di input tramite analisi sintattica per raffinarne la ricerca di una risposta inerente; utilizza il framework UIMA per l'analisi e la gestione delle informazioni semi-strutturate tramite JCAS.

Al termine di ciascun step la query viene convertita in linguaggio SPARQL tramite le API fornite da OWL-ART ed inviata a Dbpedia; la risposta ritenuta più affidabile (secondo un confronto delle risposte ottenute da Dbpedia) viene infine restituita in output.
Il core del sistema risiede nel parser di base che sfrutta le librerie di OpenNLP, così come avviene per POS-Tagger e segmenter. Il lemmatizer sfrutta le librerie di Gate Lemmatizer, mentre lo stemmer le librerie di Snowball Stemmer.
La parte grafica è stata 

## Guida all'uso

Avviando l'applicativo è possibile navigare nelle schermate QA e Settings: la prima permette di porre delle domande al sistema e vederne l'elaborato sull'area del log. Sulla sinistra invece vengono raccolte domande di esempio (selezionabili in modo da poterle eseguire) e domande recentemente poste, seguite dalle risposte ottenute dal sistema. La seconda, invece, permette di navigare tra le impostazioni del sistema:

- Result:
- - Show Advance Result: permette una stampa più dettagliata sul log che ne mostra i passaggi eseguiti per risolvere la query
- - Show Tree Representation: mostra a schermo l'albero generato dal parser relativo alla domanda posta
- - Show Tree RDF: mostra a schermo i tre sotto alberi relativi alla tripla RDF (utile per il debug)

- Parsing Method:
- - Template Parser: coincide con lo step 1 del progetto ed effettua il parsing della domanda seguendo i template predefiniti
- - Parser: coincide con lo step 3 del progetto sfruttando il parser di OpenNLP che si appoggia a stemmer e lemmatizer
- - Synonymer: permette di utilizzare il synonymer durante l'operazione di parsing della frase a supporto del metodo scelto tra i due elencati sopra

- Ontology:
- - Ontology Reference: permette di far scegliere all'utente a quale ontologia fare riferimento per la propria ricerca
- - Filter Property: permette di far scegliere all'utente quale proprietà ricercare all'interno dell'ontologia scelta

- Engine: permette di scegliere tra 3 differenti pipeline previste per affinare la ricerca a prescindere dall'ambito scelto
- - Simple: coinvolge il parser OpenNLP, l'OpenNLP POS Tagger e l'OpenNLP Segmenter
- - Stemmer: coinvolge il parser OpenNLP, l'OpenNLP POS Tagger, l'OpenNLP Segmenter e lo Snowball Stemmer
- - Lemmatizer: coinvolge il parser OpenNLP, l'OpenNLP POS Tagger, l'OpenNLP Segmenter ed il Gate Lemmatizer

Una volta decise le impostazioni preferite basterà scrivere la domanda che si vuole porre al sistema nella barra arancione presente in QA e premere Ask.

Istruzioni in caso di errori in fase di esecuzione del programma:

Potrebbe accadere che durante il pull da GitHub alcuni file all'interno della cartella Dict e la libreia Jaws.rar, vengano corrotti e quindi si rischia di ottenere degli errori sempre diversi in fase di esecuzione.    
Si consiglia quindi, come prima possibile soluzione per gli errori, quella di riscaricare le dipendenze necessarie dai vari repositori e siti.

- Jaws.rar ( https://github.com/jaytaylor/jaws )
- Dict ( https://wordnet.princeton.edu/wordnet/download/current-version/ )

## Documentazione
https://github.com/ffedericode/qamovie/blob/master/ia2_ppt3.pdf
