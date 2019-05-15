#!/bin/bash
#
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=2
#SBATCH --time=24:00:00
#SBATCH --mem=80GB
#SBATCH --job-name=JaneLiu_HW3
#SBATCH --mail-type=END
#SBATCH --mail-user=jl860@nyu.edu
#SBATCH --output=slurm_%j.out

module purge
module load jdk/1.8.0_111

cd /scratch/jl860/hw3
java -cp hw3.jar:./lib/* hw3.TwitterExtract


