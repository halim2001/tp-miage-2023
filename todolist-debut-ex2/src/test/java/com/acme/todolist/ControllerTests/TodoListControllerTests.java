package com.acme.todolist.ControllerTests;

import com.acme.todolist.adapters.persistence.TodoItemMapper;
import com.acme.todolist.adapters.persistence.TodoItemRepository;
import com.acme.todolist.application.service.GetTodoItemsService;
import com.acme.todolist.configuration.TodolistApplication;
import com.acme.todolist.domain.TodoItem;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.Instant;
import java.util.Objects;

import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = TodolistApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
public class TodoListControllerTests {
    @Autowired
    private TodoItemRepository todoItemRepository;

    @Autowired
    private GetTodoItemsService getTodoItemsService;

    @Autowired
    private TodoItemMapper mapper;

    private void createTestTodo(String id, Instant instant, String content){
        TodoItem todoItem = new TodoItem(id, instant, content);
        todoItemRepository.save(mapper.mapToTodoItemJpaEntity(todoItem));
    }

    @Test
    public void whenGetTodos_TodoIsLate_TestDisplayLate() throws Exception {
        createTestTodo("testItemMore24", Instant.now().plusSeconds(3600 * 25), "Test item de plus de 24h de retard");
        createTestTodo("testItemEqual24", Instant.now().plusSeconds(3600 * 24), "Test item d'exactement 24h de retard");
        createTestTodo("testItemMinus24", Instant.now().plusSeconds(3600 * 23), "Test item de moins de 24h de retard");
        getTodoItemsService.getAllTodoItems().forEach(todoItem -> {
            switch (todoItem.getId()) {
                case "testItemMore24":
                    Assert.assertTrue(todoItem.getContent().startsWith("[LATE!] "));
                    break;
                case "testItemEqual24":
                    Assert.assertFalse(todoItem.getContent().startsWith("[LATE!] "));
                    break;
                case "testItemMinus24":
                    Assert.assertFalse(todoItem.getContent().startsWith("[LATE!] "));
                    break;
                default:
            }
        });
    }
}