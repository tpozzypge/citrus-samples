/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.samples.todolist;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.message.MessageType;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = JmsConfig.class)
public class TodoListIT extends TestNGCitrusTestRunner {

    @Test
    @CitrusTest
    public void testHttpAddTodoEntry() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");
        variable("done", "false");

        http(httpActionBuilder -> httpActionBuilder
            .client("http://localhost:8080")
            .send()
            .post("/api/todolist")
            .messageType(MessageType.JSON)
            .contentType(ContentType.APPLICATION_JSON.getMimeType())
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));

        http(httpActionBuilder -> httpActionBuilder
            .client("http://localhost:8080")
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}"));

        http(httpActionBuilder -> httpActionBuilder
            .client("http://localhost:8080")
            .send()
            .get("/api/todo/${todoId}")
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        http(httpActionBuilder -> httpActionBuilder
            .client("http://localhost:8080")
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));
    }

    @Test
    @CitrusTest
    public void testAddTodoEntry() {
        variable("todoId", "citrus:randomUUID()");
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");
        variable("done", "false");

        send(httpActionBuilder -> httpActionBuilder
            .endpoint("jms:queue:jms.todo.inbound?connectionFactory=activeMqConnectionFactory")
            .header("_type", "com.consol.citrus.samples.todolist.model.TodoEntry")
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));

        http(httpActionBuilder -> httpActionBuilder
            .client("http://localhost:8080")
            .send()
            .get("/api/todo/${todoId}")
            .accept(ContentType.APPLICATION_JSON.getMimeType()));

        http(httpActionBuilder -> httpActionBuilder
            .client("http://localhost:8080")
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.JSON)
            .payload("{ \"id\": \"${todoId}\", \"title\": \"${todoName}\", \"description\": \"${todoDescription}\", \"done\": ${done}}"));
    }

}
