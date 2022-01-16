package coding.stream;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BasicExamples {
    @Test
    public void test_mapfilter(){
        //非终结的map操作不会运算，相当于只是一次描述
        //ReferencePipeline
        //lambda expression 匿名函数
        Stream.of(1, 2, 3, 4, 5, 6).
                map(x -> x.toString()).
                map(x -> x + x).
                map(x -> x + x + x).
                //function reference operator
                map(Integer::parseInt).
                //终结操作
                forEach(x->{
                    System.out.println(x);
                });
    }

    @Test
    public void test_mapfilterreduce(){
      //看函数签名
      var steam=
              Stream.of(1,2,3,4,5,6)
              .map(x->x*x).filter(x->x<20).
                      //BinaryOperator<T>
                              //extends BiFunction<T,T,T>
                                      //public interface BiFunction<T, U, R> {
                                      //
                                      //    /**
                                      //     * Applies this function to the given arguments.
                                      //     *
                                      //     * @param t the first function argument
                                      //     * @param u the second function argument
                                      //     * @return the function result
                                      //     */
                                      //    R apply(T t, U u);
                      //从上面可以看出reduce实际上实际上必须传入参数和返回值是同一类型的参数
                              //   public static int max(int a, int b) {
                              //        return (a >= b) ? a : b;
                              //    }
                              //都是int类型
                      //第二点看参数的英文单词 accumulator 累计器的意思
                      //可以设置初始值identity
                      //                      reduce(0,Math::max);
                      //不设置初始值得时候返回结果会包一层Optional（monad）
                              reduce(Math::min);
      System.out.println(steam.get());
    }

    @Test
    public void test_mapfilterreduce1(){
        var stream=Stream.of(1,2,3,4,5,6)
                .map(x->x*x).filter(x->x<20).reduce(Math::max);
//                orElse(0);
        System.out.println(stream.orElseGet(()->0));
    }

    @Test
    public void test_mutation(){
        var stream=Stream.of(1,3,5,2,4,6,8,10).
                //有状态的函数
                sorted();
        stream.forEach(System.out::println);
    }

    //无副作用的函数
    int add(int a,int b){
        return a+b;
    }

    //有副作用的函数
    int c=0;
    int add_c(int a, int b){
        //side effect
        c++;
        return a+b;
    }
    @Test
    public void test_peek(){
       var stream= Stream.of("one", "two", "three", "four").
                filter(e -> e.length() > 3).
                peek(e -> System.out.println("Filtered value: " + e)).
                map(String::toUpperCase).
                peek(e -> System.out.println("Mapped value: " + e)).
                collect(Collectors.toList());
       stream.forEach(System.out::println);
    }

    @Test
    public void test_flatMap(){
        //String -> Stream<R>
        //IntStream!=Stream<int>
        //int Integer
       Stream.of("My","Mine").
               flatMap(str->str.chars().
                       mapToObj(i->(char)i)).
               collect(Collectors.toSet()).forEach(System.out::println);
    }

    @Test
    public void test_parallel() throws ExecutionException, InterruptedException {
        var r=new Random();
        var list=IntStream.range(0,1_000_000).
        //map(x->(int)Math.random());
        map(x-> r.nextInt(10_000_000)).
                //int -> Integer 装箱
                boxed().
                collect(Collectors.toList());

       var t0= System.currentTimeMillis();
        //串行运算
        System.out.println(list.stream().max((a,b)->a-b).get());
        System.out.println("串行:"+(System.currentTimeMillis()-t0));


        var t1= System.currentTimeMillis();
        //并行运算 默认会开核数-1个线程 集合产生的Spliter 不断拆分
        System.out.println(list.parallelStream().max((a,b)->a-b).get());
        System.out.println("并行:"+(System.currentTimeMillis()-t1));

        var pool=  new ForkJoinPool(2);
        var t2= System.currentTimeMillis();
        System.out.println(pool.submit(()->list.parallelStream().max((a,b)->a-b)).get().get());
        System.out.println("pool:"+(System.currentTimeMillis()-t2));

        System.out.println("核心数"+(Runtime.getRuntime().availableProcessors()));
    }

    @Test
    public void test_udef(){
        //对空值的处理封装了错误
       Optional<Integer> x= Optional.empty();
       var y =x.map(s-> s * s);
       System.out.println(y.orElseGet(()->0));
    }

    @Test
    public void test_stream(){
       var result= Stream.of("Hello","World").map(x->x.length()).map(x->x+x).filter(x->x>1);
       result.forEach(System.out::println);
    }

}
