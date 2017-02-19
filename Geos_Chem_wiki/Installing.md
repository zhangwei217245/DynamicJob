# For version v10.01, Install everything that is needed.

## Create a directory for everything that might be useful

```
cd ~
mkdir GC
cd GC
```

## Download the source code:

```
git clone "git clone https://bitbucket.org/gcst/geos-chem Code.v10-01"
```

## Download GEOS-CHEM UnitTester, namely, the run directories.

```
git clone git://git.as.harvard.edu/bmy/GEOS-Chem-UnitTest UT
```

## Download any shared data directories using FTP commands

**TO BE CONTINUED**

## Ensure netCDF library installation.


### Check if netCDF library is installed with your IT staff.

<http://wiki.seas.harvard.edu/geos-chem/index.php/Installing_libraries_for_GEOS-Chem>

### If not, firstly, download Intel® Parallel Studio XE Cluster Edition (includes Fortran and C/C++)

Visit the link below:

<https://software.intel.com/en-us/qualify-for-free-software/student>


Choose Linux as your OS, and complete the registration. A installing shell script file will be downloaded, and a serial
number will be sent to your email with which you registered the account for this download.

Execute the downloaded shell script, and follow the instructions. Make sure you will install the fortran and c compiler
as well as the computing frameworks and libraries. And for simplicity, you can install them in 

```
~/intel
```

### Configure the corresponding environment variables.

Open ~/.bashrc with any text editor you like, at the end of the file, add the following lines:

```
export INTEL_HOME=/home/<username>/intel/compilers_and_libraries/linux
export LD_LIBRARY_PATH=$INTEL_HOME/lib/intel64:$INTEL_HOME/mpi/lib64:$LD_LIBRARY_PATH
source $INTEL_HOME/bin/compilervars.sh -arch intel64
```

Save the file, exit the file editor, exit the current opening terminal and re-open a new terminal running bash.

### Download and build netCDF library

```
cd ~/GC/
git clone -b netcdf-4.2 https://bitbucket.org/gcst/geos-chem-libraries GC-Lib
```

Before building netCDF library, we need to fix issue in `src/netcdf-fortran-4.2/man4/netcdf-f90.texi` and Compile netCDF library.

Open `src/netcdf-fortran-4.2/man4/netcdf-f90.texi` with any text editor, 

1. at line 2085 remove `@item`
2. at line 6982 or 6981, change `@unnumberedsubsec` to `@unnumberedsec`

Then compile the netCDF library:

```
make
make verify
```


### Configure the corresponding environment variables

Open ~/.bashrc with any text editor you like, at the end of the file, add the following lines:

```
export GC_HOME=/home/<username>/GC
export GC_BIN=$GC_HOME/GC-Lib/opt/ifort/nc4/bin
export GC_INCLUDE=$GC_HOME/GC-Lib/opt/ifort/nc4/include
export GC_LIB=$GC_HOME/GC-Lib/opt/ifort/nc4/lib
```

Save the file, exit the file editor, exit the current opening terminal and re-open a new terminal running bash.


### Compiling GEOS-Chem


Before doing so, we need to fix another issue in GEOS-Chem source code:

```
cd ~/GC/Code.v10-01
```

Open `HEMCO/Extensions/hcox_gc_RnPbBe_mod.F90` , go to line 773, change `54_hp` into `54.0_hp` .

Follow the link [Compiling GEOS-Chem](http://wiki.seas.harvard.edu/geos-chem/index.php/GEOS-Chem_Makefile_Structure#Compiling_GEOS-Chem)

Compile the GEOS-Chem executables.

For example:

```
cd ~/GC/Code.v10-01
make all -j4 GRID=4x5 MET=GEOS-5 CHEM=standard UCX=YES
```

So far, the installation is done!