package com.acme.todolist.ControllerTests;

import com.acme.todolist.adapters.persistence.TodoItemMapper;
import com.acme.todolist.adapters.persistence.TodoItemRepository;
import com.acme.todolist.application.service.GetTodoItemsService;
import com.acme.todolist.configuration.TodolistApplication;
import com.acme.todolist.domain.TodoItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;


@SpringBootTest
@AutoConfigureMockMvc
public class TodoListControllerTests {
    @Autowired
    private TodoItemRepository todoItemRepository;

    @Autowired
    private GetTodoItemsService getTodoItemsService;

    @Autowired
    private TodoItemMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    private void createTestTodo(String id, Instant instant, String content){
        TodoItem todoItem = new TodoItem(id, instant, content);
        todoItemRepository.save(mapper.mapToTodoItemJpaEntity(todoItem));
    }

    @Test
    public void whenGetTodos_TodoIsLate_TestDisplayLate() throws Exception {
        createTestTodo("testItemMore24", Instant.now().plusSeconds(3600 * 25), "Test item de plus de 24h de retard");
        createTestTodo("testItemEqual24", Instant.now().plusSeconds(3600 * 24), "Test item d'exactement 24h de retard");
        createTestTodo("testItemMinus24", Instant.now().plusSeconds(3600 * 23), "Test item de moins de 24h de retard");
        // When
        MvcResult mvcResult = mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        ObjectMapper objectMapper = new ObjectMapper();
        List<TodoItem> todoItems = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<List<TodoItem>>() {});
        for(TodoItem todoItem : todoItems) {
            switch(todoItem.getId()) {
                case "testItemMore24":
                    assertTrue(todoItem.getContent().startsWith("[LATE!] "));
                    break;
                case "testItemEqual24":
                case "testItemMinus24":
                    assertFalse(todoItem.getContent().startsWith("[LATE!] "));
                    break;
                default:
                    fail("Unexpected todo item with id " + todoItem.getId());
            }
        }
    }
}