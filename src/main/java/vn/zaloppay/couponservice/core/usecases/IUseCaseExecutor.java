package vn.zaloppay.couponservice.core.usecases;

import java.util.function.Function;

public interface IUseCaseExecutor {


    /**
     * Executes a given UseCase with the provided input and maps the output to the desired result.
     *
     * @param <R>          The final return type after mapping the output.
     * @param <I>          The input type for the UseCase (must extend UseCase.InputValues).
     * @param <O>          The output type from the UseCase (must extend UseCase.OutputValues).
     * @param useCase      The business logic (UseCase) to execute.
     * @param input    The input data for the UseCase.
     * @param outputMapper A function that transforms the raw output into the desired result.
     * @return The final result after executing the UseCase and mapping its output.
     */
    <R, I extends UseCase.InputValues, O extends UseCase.OutputValues>
    R execute(
            UseCase<I, O> useCase,
            I input,
            Function<O, R> outputMapper
    );

}
