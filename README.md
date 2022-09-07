# GNormPlus

## Introduction

GNormPlus has been developed at the NLM and is described in [1] and can be downloaded [here](https://www.ncbi.nlm.nih.gov/research/bionlp/Tools/gnormplus/). It is a tool to recognize gene mentions in the scientific biomedical literature and assign ID from [NCBI Gene](https://www.ncbi.nlm.nih.gov/gene) to them. This repository has the following purposes:
1. Add code changes to output entity mentions tagged as *FamilyName*.
2. Refactor the code to allow multi-threaded processing.
3. Refactor the code to allow its usage embedded into another Java program while the resources (gene and species dictionaries, GNR model etc.) are only loaded once at the beginning
4. Create a Maven artifact to be distributed to Maven Central for usage in a [JULIE Lab UIMA JCoRe component](https://github.com/JULIELab/jcore-base/tree/v2.6/jcore-gnormplus-ae).

## Code changes to output FamilyNames

There are two places in the code marked with a comment containing "Erik Faessler". The added conditions lead to the output of the FamilyName entities. Those entities do not receive an ID from NCBI Gene.

## Refactoring to allow multi-threaded processing

In its original form, the GNormPlus code completely operates on static fields in the `GNormPlus` class. The new class `GNPProcessingData` was created to hold all the data fields that are manipulated during the processing of a batch of files. For each file to be processed, an instance of `GNPProcessingData` is created which avoids race conditions on field access.
Of course, the access to the static fields had to be changed to access to the `GNPProcessingData` instance. Thus, the `GNPProcessingData` instance is passed to the classes that realize the individual tasks (GNR, species assignment, mapping etc.).

## Refactoring to allow usage from other Java programs without reloading the resources

The normal GNormPlus workflow consists of
* loading the dictionaries and models
* iterating over the files in the input directory
* processing each file and writing its output
* ending the program

However, we would like to keep the program running as new documents come in without a definitive knowledge about how many document there are and where to find them. This would allow us to use GNormPlus in a UIMA component. Thus, there is now a `loadResources()` method in the `GNormPlus` class that should be called once at the beginning of some processing. After that call, the dictionaries are loaded and GNormPlus is ready to process incoming documents. This can be done via the `processFile()` method. When delivering files with different names, parallel processing is possible. 


[1] Wei, C. H., Kao, H. Y., & Lu, Z. (2015). GNormPlus: An Integrative Approach for Tagging Genes, Gene Families, and Protein Domains. BioMed Research International, 2015. https://doi.org/10.1155/2015/918710