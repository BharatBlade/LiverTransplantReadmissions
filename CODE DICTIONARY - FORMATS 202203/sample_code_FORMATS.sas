/************************************************************************************************************/
/* change STARLOC TO LOCATION OF DATA
/************************************************************************************************************/
%let STARLOC=C:\STAR;

/************************************************************************************************************/
/*	for reading SAS formats into SAS dataset from Tab Delimited Files																									  											   */
/************************************************************************************************************/
/*FOR READING FORMATS PROVIDED WITHIN FOLDER "\CODE DICTIONARY - FORMATS" INTO A SAS DATASET */
/* READS ALL LIVER FORMATS - CHANGE LIVER TO APPROPRIATE ORGAN GROUP AS NAMED IN CODE DICTIONARY - FORMATS FOLDER */
PROC IMPORT OUT= liverfmts (RENAME=(VAR1=LABEL VAR2=FMTNAME VAR3=TYPE VAR4=CODE))
			DATAFILE="&STARLOC.\CODE DICTIONARY - FORMATS\Liver\liver_formats_flatfile.dat"
            DBMS=TAB REPLACE;
     GETNAMES=no;  /*if variable names exist on the file, change to YES */
     DATAROW=1;   /* row data starts */
RUN;

/* READS ONLY DIABTY FORMAT */ 
PROC IMPORT OUT= DIABTYfmt (where=(VAR2 ='DIABTY'))  /*WHERE CLAUSE BRINGS IN ONLY DIABTY FORMAT */
            DATAFILE= "&STARLOC.\CODE DICTIONARY - FORMATS\Liver\liver_formats_flatfile.dat" 
            DBMS=TAB REPLACE;
     GETNAMES=no;  /*if variable names exist on the file, change to YES */
     DATAROW=1;   /* row data starts */
RUN;

/* MACRO TO READ IN ANY INDIVIDUAL FORMAT */ 
%macro fmtread(FMTNAME);
PROC IMPORT OUT= &FNTNAME.fmt (RENAME=(VAR1=LABEL VAR2=FMTNAME VAR3=TYPE VAR4=CODE)   /*RENAMES AUTOMATIC VARIABLES */
			where=(FMTNAME ="&FMTNAME." ))  /*WHERE CLAUSE BRINGS IN ONLY A SINGLE FORMAT */
            DATAFILE= "&STARLOC.\CODE DICTIONARY - FORMATS\Liver\liver_formats_flatfile.dat" 
            DBMS=TAB REPLACE;
     GETNAMES=no;  /*if variable names exist on the file, change to YES */
     DATAROW=1;   /* row data starts */
RUN;
/* CHANGE FMTNAME TO FORMAT WITHIN DOCUMENTATION FILE */
/* GET FORMAT NAME FROM THE DOCUMENTATION FILE */
/* DIABTY EXAMPLE PROVIDED */
%fmtread(DIABTY);