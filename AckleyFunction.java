import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.Factory;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import static io.jenetics.engine.Limits.*;
import static java.lang.Math.cos;
import static java.lang.Math.PI;

public class AckleyFunction {
    static final int CASE_NO = 2;

    // Definition of the fitness function.e
    private static double fitness(Genotype<DoubleGene> gt) {
        double fitness=0.0d;
        double X1, X2;

        X1= gt.chromosome().get(0).doubleValue();
        X2= gt.chromosome().get(1).doubleValue();

        //fitness function
        fitness = -20.0*Math.exp(-0.2*Math.sqrt(0.5 * ((X1*X1)+(X2*X2))))-Math.exp(0.5*(cos(2*PI*(double)X1)+cos(2*PI*(double)X2)))+Math.exp(1.0)+20;
        //System.out.println(fitness);
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

            default -> throw new IllegalStateException("Enter a Correct Case No");
        };
    }


    public static void main(String[] args) {
        // 1.) Define the genotype (factory) suitable for the problem.
        Factory<Genotype<DoubleGene>> gtfDouble = Genotype.of(DoubleChromosome.of(-5, 5, 2));

        List<String[]> bestPhenotypeForEveryGeneration = new ArrayList<>();
        // engine creation
        Engine.Builder<DoubleGene, Double> builder = Engine.builder(AckleyFunction::fitness, gtfDouble);
        Engine<DoubleGene, Double> engine = selectedCase(builder);

        // 4.) Start the execution (evolution) and collect the result.
        final EvolutionStatistics<Double, DoubleMomentStatistics> statisticsDouble = EvolutionStatistics.ofNumber();

        engine.stream()
        //.limit(bySteadyFitness(50))
        .limit(byFitnessConvergence(10,30, 10E-4))
        //.limit(200)
        .peek(statisticsDouble)
        .forEach(
                //result -> System.out.println(result.bestPhenotype())
                result -> {
//                    bestPhenotypeForEveryGeneration.add(result.bestPhenotype());
                    List<String> generationOutput = new ArrayList<>();

                    String generation = String.valueOf(result.generation());
                    String bestGenotype = result.bestPhenotype().genotype().chromosome().toString();
                    String worstGenotype = result.worstPhenotype().genotype().chromosome().toString();
                    String worstFitness = result.worstFitness().toString();
                    String bestFitness = result.bestPhenotype().fitness().toString();
                    String averageFitness = String.valueOf(
                            result.population().stream().mapToDouble(Phenotype::fitness).average().orElseThrow()
                    );

                    generationOutput.add(generation);
                    generationOutput.add(worstGenotype);
                    generationOutput.add(worstFitness);
                    generationOutput.add(bestGenotype);
                    generationOutput.add(bestFitness);
                    generationOutput.add(averageFitness);

                    String[] row = new String[generationOutput.size()];
                    generationOutput.toArray(row);

                    bestPhenotypeForEveryGeneration.add(row);
                });


       
        System.out.println("Results :\n" + statisticsDouble);

    }

}
