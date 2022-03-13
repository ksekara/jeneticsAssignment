import com.opencsv.CSVWriter;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.Factory;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import static io.jenetics.engine.Limits.*;
import static java.lang.Math.cos;
import static java.lang.Math.PI;

/**
 * @author 218541J D.K Sekarage
 * @author 218552T S.U Yapa
 **/

public class AckleyFunction {
    static final int CASE_NO = 3;

    // Definition of the fitness function.e
    private static double fitness(Genotype<DoubleGene> gt) {
        double fitness=0.0d;
        double X1, X2;

        X1= gt.chromosome().get(0).doubleValue();
        X2= gt.chromosome().get(1).doubleValue();

        //fitness function
        fitness = -20.0*Math.exp(-0.2*Math.sqrt(0.5 * ((X1*X1)+(X2*X2))))-Math.exp(0.5*(cos(2*PI*(double)X1)+cos(2*PI*(double)X2)))+Math.exp(1.0)+20;

        return fitness;
    }
    private static Engine<DoubleGene, Double> selectedCase(Engine.Builder<DoubleGene, Double> builder){
        return switch (CASE_NO){
            case 1-> builder
                    .populationSize(60)
                    .selector(new TournamentSelector<>())
                    .optimize(Optimize.MINIMUM)
                    .alterers(
                            new Mutator<>(0.5),
                            new SinglePointCrossover<>(0.9)
                    )
                    .survivorsSelector(new EliteSelector<>())
                    .build();

            case 2-> builder
                    .populationSize(100)
                    .selector(new RouletteWheelSelector<>())
                    .optimize(Optimize.MINIMUM)
                    .alterers(
                            new Mutator<>(0.5),
                            new SinglePointCrossover<>(0.9)
                    )
                    .survivorsSelector(new TournamentSelector<>())
                    .build();

            case 3-> builder
                    .populationSize(200)
                    .selector(new StochasticUniversalSelector<>())
                    .optimize(Optimize.MINIMUM)
                    .alterers(
                            new Mutator<>(0.5),
                            new MultiPointCrossover<>(0.9)
                    )
                    .survivorsSelector(new TruncationSelector<>())
                    .build();

            default -> throw new IllegalStateException("Enter a Correct Case No");
        };
    }


    public static void main(String[] args) {
        // Define the genotype (factory) suitable for the problem.
        Factory<Genotype<DoubleGene>> gtfDouble = Genotype.of(DoubleChromosome.of(-5, 5, 2));

        List<String[]> recordForeveryGen = new ArrayList<>();
        // engine creation
        Engine.Builder<DoubleGene, Double> builder = Engine.builder(AckleyFunction::fitness, gtfDouble);
        Engine<DoubleGene, Double> engine = selectedCase(builder);

        // Start the execution (evolution) and collect the result.
        final EvolutionStatistics<Double, DoubleMomentStatistics> statisticsDouble = EvolutionStatistics.ofNumber();

        engine.stream()
        //.limit(bySteadyFitness(50))
        //.limit(byFitnessConvergence(10,30, 10E-4))
        .limit(200)
        .peek(statisticsDouble)
        .forEach(
                  result -> {
                    List<String> outRecord = new ArrayList<>();

                    String generation = String.valueOf(result.generation());
                    String bestChromosome = result.bestPhenotype().genotype().chromosome().toString();
                    String bestFitnessVal = result.bestPhenotype().fitness().toString();
                    String avgFitness = String.valueOf(
                            result.population().stream().mapToDouble(Phenotype::fitness).average().orElseThrow()
                    );

                      outRecord.add(generation);
                      outRecord.add(bestChromosome);
                      outRecord.add(bestFitnessVal);
                      outRecord.add(avgFitness);

                    String[] row = new String[outRecord.size()];
                      outRecord.toArray(row);

                    recordForeveryGen.add(row);
                });


        AckleyFunction.createCSV(recordForeveryGen, "test-limit.csv");
        System.out.println("Results :\n" + statisticsDouble);


    }

    public static void createCSV(List<String[]> data, String filePath) {

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filePath));
            for (String[] row : data) {
                writer.writeNext(row);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
