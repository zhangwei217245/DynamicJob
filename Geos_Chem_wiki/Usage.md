# Prerequisite:

## Directories storing GEOS-CHEM and rundirs

* Parent Directory:

```
/home/liu40/GC
```

* There are two different versions of GEOS-CHEM software, version v10.01 is the current release while version v11 is the so-called high performance version. Currently, only version v10.01 is what we've tested, it works.
* Source Code for Version 10.01 

```
/home/liu40/GC/Code.v10-01
```

* UnitTester for version 10-01

```
/home/liu40/GC/UT
```

* Source Code for Version 11 (High Performance Version)

```
/home/liu40/GC/GCCode.v11
```

* UnitTester for version 11

```
/home/liu40/GC/UT.v11
```

* All Experimental Data is linked or stored at 

```
/home/liu40/GC/ExtData
```
# Steps for Running GEOS-CHEM v10.01

## Login to the hrothgar.
```
ssh liu40@hrothgar.hpcc.ttu.edu
```

## Change current working directory
```
cd /home/liu40/GC/UT/perl
```

## Edit `CopyRunDirs.input`
```
vim CopyRunDirs.input
```
Now you can see the following content in the text file:

```
#------------------------------------------------------------------------------
#                  GEOS-Chem Global Chemical Transport Model                  !
#------------------------------------------------------------------------------
#BOP
#
# !DESCRIPTION: Input file that specifies configuration for creating and
#  copying a run directory from the UnitTester.
#\\
#\\
# !REMARKS:
#  Customize the run directory for your system by specifying these values:
#  -------------------------------------------------------------------
#  VERSION     : A tag used to identify this Unit Test (e.g. v10-01h)
#  DESCRIPTION : A short description of this file's run dir copy configuration
#  COPY_PATH   : Local path where run directory will be copied to
#  DATA_ROOT   : Root GEOS-Chem data directory
#  HEMCO_ROOT  : Root directory where HEMCO emissions data files are stored
#  RUN_ROOT    : Unit test run directories are subdirectories of RUN_ROOT
#  RUN_DIR     : Individual unit test run directory path
#  PERL_DIR    : Unit Test perl script directory (i.e. this directory)
#  COPY_CMD    : Unix copy command with optional tags
#
# !REVISION HISTORY:
#  18 Mar 2015 - R. Yantosca - Initial version
#  19 Mar 2015 - E. Lundgren - Simplify content for only copying run dirs
#  19 May 2015 - R. Yantosca - Now can specify VERBOSE and WARNINGS options
#EOP
#------------------------------------------------------------------------------
#
# !INPUTS:
#
   VERSION     : v10-01
   DESCRIPTION : Create run directory from UnitTest
   COPY_PATH   : {HOME}/GC/rundirs
   DATA_ROOT   : /home/liu40/GC/ExtData
   HEMCO_ROOT  : {DATAROOT}/HEMCO
   RUN_ROOT    : {HOME}/GC/UT/runs
   RUN_DIR     : {RUNROOT}/{RUNDIR}
   PERL_DIR    : {HOME}/UT/perl
   COPY_CMD    : cp -rfL
   VERBOSE     : 0
   WARNINGS    : 1
#
# !RUNS:
#  Specify the runs directories that you want to copy below.
#  Here we provide a few examples, but you may copy additional entries from
#  UnitTest.input and modify the dates as needed. You can deactivate copying
#  run certain directories by commenting them out with "#".
#
#--------|-----------|------|------------|------------|------------|---------|
# MET    | GRID      | NEST | SIMULATION | START DATE | END DATE   | EXTRA?  |
#--------|-----------|------|------------|------------|------------|---------|
#  geosfp   4x5         -      benchmark    2013070100   2013080100   -
#  geosfp   4x5         -      tropchem     2013070100   2013070101   -
#  geosfp   4x5         -      soa          2013070100   2013070101   -
#  geosfp   4x5         -      soa_svpoa    2013070100   2013070101   -
#  geosfp   4x5         -      UCX          2013070100   2013070101   -
#  geosfp   4x5         -      RRTMG        2013070100   2013070101   -
#  geosfp   4x5         -      RnPbBe       2013070100   2013070101   -
#  geosfp   4x5         -      Hg           2013070100   2013070101   -
#  geosfp   4x5         -      POPs         2013070100   2013070101   -
#  geosfp   4x5         -      CH4          2013070100   2013070101   -
#  geosfp   4x5         -      tagO3        2013070100   2013070101   -
#  geosfp   4x5         -      tagCO        2013070100   2013070101   -
#  geosfp   2x25        -      CO2          2013070100   201307010030 -
  geos5   4x5         -      aerosol      2013010100   2013010101   -
#  geosfp   025x03125   ch     tropchem     2013070100   201307010010 -
#  geosfp   025x03125   na     tropchem     2013070100   201307010010 -
!END OF RUNS:
#EOP
#------------------------------------------------------------------------------
```
* Note:
0. Follow the instructions in the file to change different options accordingly.
1. Every line can be marked as a comment by typing a '#' at the beginning of the line. 
2. Press `Fn + <-` for getting to the beginning of the line, Press `Fn + ->` for getting to the very end of the line. 
3. Press 'i' for entering into the editing mode. Press 'Esc' for getting back to the command mode. Press 'v' for getting into the block mode.
4. In the editing mode, move your cursor by pressing any of the direction keys. Type whatever you want, and then you get the characters typed.
5. In the command mode, the following commands are potentially useful:
    1. `Shift+G`:  Go to the bottom line of the entire file.  `:1` : Go to the first line of the entire file.
    2. `:wq`: Write the buffer into the file and quit vim. `:w` : Only write the buffer into the file. `:q!`: quit the vim without saving any changes made in the buffer.
    3. `u` for undo.
    4. `dd` for cutting a line. `yy` for copy a line. `p` for pasting everything that is copied or cut.
    5. `v` for block mode. While in the block mode, use any direction keys to select the text block that you want to copy or cut, then press `d` or `y` to actually cut or copy them, then press `p` for pasting.
    6. `ctrl+v` for visual block mode. In this mode, use any direction keys to select any rectangle block, then you can also do the cut/copy/paste as you want.

## Run `./gcCopyRunDirs`
```
./gcCopyRunDirs
```
If you get the following error output:
```
%%%%%%%%% GEOS-Chem COPY RUN DIRECTORIES %%%%%%%%%%%%%%%%%%%%%
%%% Version ID  : v10-01
%%% Description : Create run directory from UnitTest
%%%
%%% Copying geos5_4x5_aerosol to /home/liu40/GC/rundirs/geos5_4x5_aerosol
%%% ERROR!  <RUNDIR> already exists!
%%% Delete or move /home/liu40/GC/rundirs/geos5_4x5_aerosol before proceeding!
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
```
Please run:
```
rm -rf <RUNDIR>
```
Then run the command again:
```
./gcCopyRunDirs
```

## Go to `/home/liu40/GC/rundirs`
```
cd /home/liu40/GC/rundirs
ls -l
```
Then you will find the generated directory in this directory. Enter into the generated directory by `cd` command.

## Compile the program
```
make -j4
```

## Note:
1. Rundir is the directory generated by `gcCopyRunDirs`
2. In the rundir, you can find input.geos in which you can customize you need against different tracers.
2. In the HEMCO_Config.rc, you can configure what HEMCO data you wanna use.