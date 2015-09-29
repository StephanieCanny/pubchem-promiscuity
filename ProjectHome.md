# Compound Promiscuity Analysis using PubChem #

# Go To [PubChem Promiscuity Website](http://chemutils.florida.scripps.edu:8080/pcpromiscuity/). #

## Summary ##
PubChem Promiscuity uses NCBI Entrez (eUtils) web services and JOELib. It provides PubChem promiscuity counts in a variety of categories and compound descriptors, including functional group detection of PAINS.

## Implementation ##
This tool was developed using [Java](http://www.oracle.com/technetwork/java/index.html) and the previously published [PubChemDB](http://bioinformatics.oxfordjournals.org/content/27/5/741.short?rss=1) Java API for eUtils functions. [JOELib](http://sourceforge.net/projects/joelib/) was used for SMARTS querying of [PAINS](http://pubs.acs.org/doi/abs/10.1021/jm901137j) groups within compound smiles. PAINS SMARTS were obtained from [here](http://blog.rguha.net/?p=850).

![http://mlpcn.florida.scripps.edu/images/stories/scripps/PCPromiscuity.png](http://mlpcn.florida.scripps.edu/images/stories/scripps/PCPromiscuity.png)