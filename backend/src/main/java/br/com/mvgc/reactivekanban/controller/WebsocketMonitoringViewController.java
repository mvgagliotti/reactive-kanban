package br.com.mvgc.reactivekanban.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Controller
public class WebsocketMonitoringViewController {

    @GetMapping("/ws-monitoring/{id}")
    public String boardView(@PathVariable("id") UUID boardId, Model model) {
        model.addAttribute("id", boardId.toString());
        return "websocket-monitoring.html";
    }

}
