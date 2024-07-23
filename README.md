# Spring Boot REST Controller

Esse repositório implementa uma controladora REST simples.

Para Implementar o Exemplo crie um projeto Spring Boot utilizando o [Initalizr]().

Ao contrário do modelo MVC, onde setamos os parâmetros e renderizamos uma View, uma controladora REST trabalha com um objeto transacional, normalmente em JSON ou XML.
Sendo assim, não teremos templates HTML em nosso código, o mesmo responderá as requisições com objetos transacionais.

Para o exemplo comece pela configuração das dependêncidas.
Adicione a seguintes dependências:

``` xml
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.34</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
```

Criado o novo projeto, iremos começar pela nossa entidade que será mantida.
Posteriormente iremos criar uma Controller que disponibilizará nosso dados via requisições WEB (PUT, GET, POST e DELETE)

``` java
package com.springboot.restcontroller.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String name;
    String course;
    String email;

    public Student(String name, String course, String email) {
        this.name = name;
        this.course = course;
        this.email = email;
    }
}
```
Logo em seguida temos criar a interface, que fará a manipulação de nossa database:
Com essa interface podemos:

    - Criar novos estudantes;
    - Alterar estudantes;
    - Deletar estudantes;
    - Encontrar estudantes (somente um, todos, ou por qualquer um de seus atributos);

``` java
package com.springboot.restcontroller.Repository;

import com.springboot.restcontroller.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Integer> {
}
```
Em seguida iremos adicionar alguns registros a nossa database H2.
Isso se faz necessário, em virtude dessa database ser em memória, ou seja, a cada vez que nossa aplicação é reinciada, os dados são perdidos.

O Spring Boot através da anotação *@Configuration* e *@Bean*, quando a aplicação é inciada é capaz de detectar que essa porção de código deve ser executada durante a inicialização da aplicação.

``` java
package com.springboot.restcontroller.Configuration;

import com.springboot.restcontroller.Model.Student;
import com.springboot.restcontroller.Repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabase {
    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(StudentRepository repository) {
        return args -> {
            log.info("Preloading " + repository.save(new Student("John Doe", "Enginering", "john@mail.com.ar")));
            log.info("Preloading " + repository.save(new Student("Jane Doe", "Arts", "jane@mail.com.ar")));
            log.info("Preloading " + repository.save(new Student("Peter Dilan", "History", "dilan@mail.com.ar")));
        };
    }
}
```

Em seguinda precisamos de uma controller, que nos possibilite acessar nosso repositório de dados.
```java
package com.springboot.restcontroller.Controller;

import com.springboot.restcontroller.Exception.StudentNotFoundException;
import com.springboot.restcontroller.Model.Student;
import com.springboot.restcontroller.Repository.StudentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StudentController {

    private final StudentRepository repository;

    StudentController(StudentRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/students")
    List<Student> all() {
        return repository.findAll();
    }

    @PostMapping("/students")
    Student newStudent(@RequestBody Student student) {
        return repository.save(student);
    }

    @GetMapping("/students/{id}")
    Student getStudent(@PathVariable Integer id) throws StudentNotFoundException {
        return repository.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
    }

    @PutMapping("/students/{id}")
    Student updateStudent(@PathVariable Integer id, @RequestBody Student newStudent) throws StudentNotFoundException {
        return repository.findById(id).map(student -> {
            student.setName(newStudent.getName());
            student.setCourse(newStudent.getCourse());
            student.setEmail(newStudent.getEmail());
            return repository.save(student);
        }).orElseGet(() -> {
            return repository.save(newStudent);
        });
    }

    @DeleteMapping("/students/{id}")
    void deleteStudent(@PathVariable Integer id) {
        repository.deleteById(id);
    }
}
```

Após essa abordagem inicial iremos melhorar nosso exemplo
Adicionaremos a dependência HATEOAS que irá organizar nossos retornos para os possíveis consumidores.
Com o HATEOAS podemos criar links para nossos objetos de forma simples e fácil.

Para implementarmos esse links devemos adicionar primeiramente a dependência HATEOAS:

``` xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```

Em seguida modificaremos nossa controller, para que as nossas entidades listadas sejam linkadas entre si:

``` java
    @GetMapping("/students/{id}")
    EntityModel<Student> getStudent(@PathVariable Integer id) {
        Student student;
        try {
            student = repository.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
        } catch (StudentNotFoundException e) {
            throw new RuntimeException(e);
        }
        return EntityModel.of(student,
                linkTo(methodOn(StudentController.class).getStudent(id)).withSelfRel(),
                linkTo(methodOn(StudentController.class).getAllStudents()).withSelfRel());
    }
```

Aqui está nosso método para obter um estudante por id.

A EntityModel possibilita que o objeto student seja representado, e ainda os links que desejarmos estejam contidos no mesmo objeto.

O método linkTo() forma o link para o próprio student, bem como para o resultado com todos os students.

``` java
    @GetMapping("/students")
    CollectionModel<EntityModel<Student>> getAllStudents() {
        List<EntityModel<Student>> students = repository.findAll().stream().
                map(student -> EntityModel.of(student,
                        linkTo(methodOn(StudentController.class).getStudent(student.getId())).withSelfRel(),
                        linkTo(methodOn(StudentController.class).getAllStudents()).withRel("students")))
                .collect(Collectors.toList());
        return CollectionModel.of(students, linkTo(methodOn(StudentController.class).getAllStudents()).withSelfRel());
    }
```

Aqui como ficam todos os resultados exibidos.
Aqui utilizamos uma CollectionModel, que agrupa todos as EntityModel.
De forma geral, cada entidade terá o link para ela mesma, para todas as entidades, e para a collection.

Referências:

[Building REST services with Spring](https://spring.io/guides/tutorials/rest#_the_story_so_far)
