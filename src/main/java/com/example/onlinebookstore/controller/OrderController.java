package com.example.onlinebookstore.controller;

import com.example.onlinebookstore.dto.order.OrderRequestDto;
import com.example.onlinebookstore.dto.order.OrderResponseDto;
import com.example.onlinebookstore.dto.order.OrderUpdateStatusDto;
import com.example.onlinebookstore.dto.orderitem.OrderItemResponseDto;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.service.order.OrderService;
import com.example.onlinebookstore.service.orderitem.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order management", description = "Endpoints for managing orders")
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final OrderItemService orderItemService;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Create a new order")
    public OrderResponseDto addOrder(@RequestBody OrderRequestDto requestDto,
                                     Authentication authentication) {
        return orderService.addOrder((User) authentication.getPrincipal(), requestDto);
    }

    @GetMapping
    @Operation(summary = "Get all order", description = "Get a list of all order")
    public List<OrderResponseDto> getAll(Authentication authentication, Pageable pageable) {
        return orderService.findAll((User) authentication.getPrincipal(), pageable);
    }

    @GetMapping("/{orderId}/items")
    @Operation(summary = "Get all by order item", description = "Get a list of all order item")
    public List<OrderItemResponseDto> getAllByOrderItem(Authentication authentication,
                                                        @PathVariable Long orderId) {
        return orderItemService.findByIdAndUser(orderId, (User) authentication.getPrincipal());
    }

    @GetMapping("/{orderId}/items/{itemId}")
    public OrderItemResponseDto getOrderIdAndItemId(Authentication authentication,
                                                    @PathVariable Long orderId,
                                                    @PathVariable Long itemId) {
        return orderItemService.findByUserAndIdAndOrderItems_Id(
                (User) authentication.getPrincipal(), orderId, itemId);
    }

    @PatchMapping("/{orderId}")
    @Operation(summary = "Update status", description = "Update status by order id")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public OrderResponseDto updateOrderStatusById(
                                                  @PathVariable Long orderId, @RequestBody
                                                  OrderUpdateStatusDto orderUpdateStatusDto) {
        return orderService.updateOrderStatusById(orderId, orderUpdateStatusDto);
    }
}
