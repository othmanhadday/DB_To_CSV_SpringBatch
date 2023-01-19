package com.othmane.DbToCsv.batch;

import com.othmane.DbToCsv.entities.Film;
import com.othmane.DbToCsv.mapper.FilmMapper;
import com.othmane.DbToCsv.processor.FilmProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private FilmProcessor filmProcessor;
    @Autowired
    private FilmMapper filmMapper;

    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("job_films").start(step()).build();
    }

    @Bean
    @Async
    public Step step() throws Exception {
        return stepBuilderFactory.get("step_films").<Film, Film>chunk(100).reader(dbReader(null, null)).processor(processor()).writer(fileWriter()).taskExecutor(taskExecutor()).build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(64);
        executor.setMaxPoolSize(64);
        executor.setQueueCapacity(64);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("MultiThreaded-");
        return executor;
    }

    @Bean
    @StepScope
    public ItemStreamReader<Film> dbReader(@Value("#{jobParameters[date]}") Date date,
                                           @Value("#{jobParameters[film_rating]}") String film_rating)
            throws Exception {

        System.out.println(date);
        System.out.println(film_rating);

        return (ItemStreamReader<Film>) itemStreamReader(filmMapper, "select * ",
                "from film",
                "where id_generated='" + film_rating + "'");
    }

    @Bean
    public ItemProcessor<Film, Film> processor() {

        return new ItemProcessor<Film, Film>() {
            @Override
            public Film process(Film film) throws Exception {
                return film;
            }
        };

    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Film> fileWriter() {
        FlatFileItemWriter<Film> writer = new FlatFileItemWriter<Film>();
        writer.setResource(new FileSystemResource("data/output.csv"));

        writer.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write("Id, title, description, last_update, length, rating, release_year, rental_rate, replacement_cost, special_features");
            }
        });

        DelimitedLineAggregator<Film> lineAggregator = new DelimitedLineAggregator<Film>();
        lineAggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<Film> fieldExtractor = new BeanWrapperFieldExtractor<Film>();
        fieldExtractor.setNames(new String[]{"id", "title", "description", "last_update", "length", "rating", "release_year", "rental_rate", "replacement_cost", "special_features"});

        lineAggregator.setFieldExtractor(fieldExtractor);

        writer.setLineAggregator(lineAggregator);
        return writer;
    }

    @StepScope
    public ItemStreamReader<? extends Object> itemStreamReader(RowMapper rowMapper, String select, String from, String where) throws Exception {
        JdbcPagingItemReader<Object> reader = new JdbcPagingItemReader<Object>();
        reader.setDataSource(dataSource);
        final SqlPagingQueryProviderFactoryBean sqlPagingQueryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();
        sqlPagingQueryProviderFactoryBean.setDataSource(dataSource);
        // sqlPagingQueryProviderFactoryBean.setDataSource(dataSource);
        sqlPagingQueryProviderFactoryBean.setSelectClause(select);
        sqlPagingQueryProviderFactoryBean.setFromClause(from);
        // sqlPagingQueryProviderFactoryBean.setWhereClause(where);
        sqlPagingQueryProviderFactoryBean.setSortKey("id");
        reader.setQueryProvider(sqlPagingQueryProviderFactoryBean.getObject());
        reader.setPageSize(1000000);
        reader.setRowMapper(rowMapper);
        reader.afterPropertiesSet();
        reader.setSaveState(false);
        return reader;
    }


}
