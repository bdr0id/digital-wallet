package com.boit_droid.wallet.controller;

import com.boit_droid.wallet.dto.request.NotificationRequest;
import com.boit_droid.wallet.dto.response.CustomApiResponse;
import com.boit_droid.wallet.dto.response.NotificationResponse;
import com.boit_droid.wallet.dto.response.PagedResponse;
import com.boit_droid.wallet.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notifications and messaging system")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
        summary = "Send notification to user",
        description = "Creates and sends a new notification to a specific user. Supports various notification types, priorities, and delivery channels.",
        tags = {"Notifications"}
    )
   @ApiResponses(value = {
       @ApiResponse(
           responseCode = "201",
           description = "Notification sent successfully",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "Success Response",
                   value = """
                   {
                       "success": true,
                       "message": "Notification sent successfully",
                       "requestId": "req_123456789",
                       "data": {
                           "notificationId": "notif_987654321",
                           "userId": "user123",
                           "title": "Transaction Alert",
                           "message": "Your transfer of $100 was successful",
                           "type": "TRANSACTION",
                           "status": "SENT",
                           "priority": "MEDIUM",
                           "channel": "IN_APP",
                           "isRead": false,
                           "isDelivered": true,
                           "createdAt": "2024-01-15T10:30:00Z"
                       },
                       "timestamp": "2024-01-15T10:30:00Z"
                   }
                   """
               )
           )
       ),
       @ApiResponse(
           responseCode = "400",
           description = "Invalid request data or validation failed",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
               examples = @ExampleObject(
                   name = "Validation Error",
                   value = """
                   {
                       "success": false,
                       "message": "Validation failed",
                       "requestId": "req_123456789",
                       "errors": ["Title is required", "Invalid notification type"],
                       "timestamp": "2024-01-15T10:30:00Z"
                   }
                   """
               )
           )
       ),
       @ApiResponse(
           responseCode = "404",
           description = "User not found",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse")
           )
       )
   })
    @PostMapping("/users/{userId}/send")
    public ResponseEntity<CustomApiResponse<NotificationResponse>> sendNotification(
            @Parameter(
                description = "Unique identifier of the user to send notification to",
                required = true,
                example = "user123"
            )
            @PathVariable String userId,
            
            @RequestBody(
                description = "Notification details including title, message, type, and delivery preferences",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NotificationRequest.class),
                    examples = @ExampleObject(
                        name = "Transaction Notification",
                        value = """
                        {
                            "title": "Transaction Alert",
                            "message": "Your transfer of $100 to John Doe was successful",
                            "type": "TRANSACTION",
                            "priority": "MEDIUM",
                            "channel": "IN_APP",
                            "relatedTransactionId": "txn_123456",
                            "requiresAcknowledgment": false
                        }
                        """
                    )
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody NotificationRequest request) {
        
        log.info("Sending notification to user: {}", userId);
        CustomApiResponse<NotificationResponse> response = notificationService.sendNotification(userId, request);
        
        HttpStatus status = response.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Get user notifications",
        description = "Retrieves all notifications for a specific user with pagination support. Returns both read and unread notifications ordered by creation date.",
        tags = {"Notifications"}
    )
   @ApiResponses(value = {
       @ApiResponse(
           responseCode = "200",
           description = "Notifications retrieved successfully",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "Success Response",
                   value = """
                   {
                       "success": true,
                       "message": "Notifications retrieved successfully",
                       "requestId": "req_123456789",
                       "data": {
                           "content": [
                               {
                                   "notificationId": "notif_001",
                                   "userId": "user123",
                                   "title": "Transaction Alert",
                                   "message": "Your transfer was successful",
                                   "type": "TRANSACTION",
                                   "priority": "MEDIUM",
                                   "isRead": false,
                                   "createdAt": "2024-01-15T10:30:00Z"
                               }
                           ],
                           "totalElements": 25,
                           "totalPages": 2,
                           "size": 20,
                           "number": 0,
                           "first": true,
                           "last": false
                       },
                       "timestamp": "2024-01-15T10:30:00Z"
                   }
                   """
               )
           )
       ),
       @ApiResponse(
           responseCode = "400",
           description = "Invalid pagination parameters",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse")
           )
       ),
       @ApiResponse(
           responseCode = "404",
           description = "User not found",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse")
           )
       )
   })
    @GetMapping("/users/{userId}")
    public ResponseEntity<CustomApiResponse<PagedResponse<NotificationResponse>>> getNotifications(
            @Parameter(
                description = "Unique identifier of the user whose notifications to retrieve",
                required = true,
                example = "user123"
            )
            @PathVariable String userId,
            
            @Parameter(
                description = "Page number for pagination (0-based)",
                example = "0"
            )
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(
                description = "Number of notifications per page (max 100)",
                example = "20"
            )
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Retrieving notifications for user: {}, page: {}, size: {}", userId, page, size);
        CustomApiResponse<PagedResponse<NotificationResponse>> response =
                notificationService.getNotifications(userId, page, size);
        
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Get unread notifications",
        description = "Retrieves only unread notifications for a specific user with pagination support. Useful for displaying notification badges and alerts.",
        tags = {"Notifications"}
    )
   @ApiResponses(value = {
       @ApiResponse(
           responseCode = "200",
           description = "Unread notifications retrieved successfully",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "Success Response",
                   value = """
                   {
                       "success": true,
                       "message": "Unread notifications retrieved successfully",
                       "requestId": "req_123456789",
                       "data": {
                           "content": [
                               {
                                   "notificationId": "notif_002",
                                   "userId": "user123",
                                   "title": "Security Alert",
                                   "message": "New login detected from unknown device",
                                   "type": "SECURITY",
                                   "priority": "HIGH",
                                   "isRead": false,
                                   "createdAt": "2024-01-15T11:00:00Z"
                               }
                           ],
                           "totalElements": 5,
                           "totalPages": 1,
                           "size": 20,
                           "number": 0,
                           "first": true,
                           "last": true
                       },
                       "timestamp": "2024-01-15T11:00:00Z"
                   }
                   """
               )
           )
       ),
       @ApiResponse(
           responseCode = "400",
           description = "Invalid pagination parameters",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse")
           )
       ),
       @ApiResponse(
           responseCode = "404",
           description = "User not found",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse")
           )
       )
   })
    @GetMapping("/users/{userId}/unread")
    public ResponseEntity<CustomApiResponse<PagedResponse<NotificationResponse>>> getUnreadNotifications(
            @Parameter(
                description = "Unique identifier of the user whose unread notifications to retrieve",
                required = true,
                example = "user123"
            )
            @PathVariable String userId,
            
            @Parameter(
                description = "Page number for pagination (0-based)",
                example = "0"
            )
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(
                description = "Number of notifications per page (max 100)",
                example = "20"
            )
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Retrieving unread notifications for user: {}, page: {}, size: {}", userId, page, size);
        CustomApiResponse<PagedResponse<NotificationResponse>> response =
                notificationService.getUnreadNotifications(userId, page, size);
        
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Mark notification as read",
        description = "Marks a specific notification as read by the user. This updates the notification status and sets the read timestamp.",
        tags = {"Notifications"}
    )
   @ApiResponses(value = {
       @ApiResponse(
           responseCode = "200",
           description = "Notification marked as read successfully",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "Success Response",
                   value = """
                   {
                       "success": true,
                       "message": "Notification marked as read successfully",
                       "requestId": "req_123456789",
                       "data": "Notification notif_001 marked as read",
                       "timestamp": "2024-01-15T12:00:00Z"
                   }
                   """
               )
           )
       ),
       @ApiResponse(
           responseCode = "400",
           description = "Invalid notification ID or notification already read",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse")
           )
       ),
       @ApiResponse(
           responseCode = "404",
           description = "Notification not found",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse")
           )
       )
   })
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<CustomApiResponse<String>> markNotificationAsRead(
            @Parameter(
                description = "Unique identifier of the notification to mark as read",
                required = true,
                example = "notif_987654321"
            )
            @PathVariable String notificationId) {
        
        log.info("Marking notification as read: {}", notificationId);
        CustomApiResponse<String> response = notificationService.markNotificationAsRead(notificationId);
        
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Mark all notifications as read",
        description = "Marks all notifications for a specific user as read. This is useful for clearing notification badges and marking bulk notifications as read.",
        tags = {"Notifications"}
    )
   @ApiResponses(value = {
       @ApiResponse(
           responseCode = "200",
           description = "All notifications marked as read successfully",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "Success Response",
                   value = """
                   {
                       "success": true,
                       "message": "All notifications marked as read successfully",
                       "requestId": "req_123456789",
                       "data": "5 notifications marked as read for user user123",
                       "timestamp": "2024-01-15T12:30:00Z"
                   }
                   """
               )
           )
       ),
       @ApiResponse(
           responseCode = "400",
           description = "Invalid user ID or operation failed",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse")
           )
       ),
       @ApiResponse(
           responseCode = "404",
           description = "User not found",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse")
           )
       )
   })
    @PutMapping("/users/{userId}/read-all")
    public ResponseEntity<CustomApiResponse<String>> markAllNotificationsAsRead(
            @Parameter(
                description = "Unique identifier of the user whose notifications to mark as read",
                required = true,
                example = "user123"
            )
            @PathVariable String userId) {
        
        log.info("Marking all notifications as read for user: {}", userId);
        CustomApiResponse<String> response = notificationService.markAllNotificationsAsRead(userId);
        
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Get notification counts",
        description = "Retrieves notification counts for a specific user, including total, unread, and counts by type. Useful for displaying notification badges and dashboard statistics.",
        tags = {"Notifications"}
    )
   @ApiResponses(value = {
       @ApiResponse(
           responseCode = "200",
           description = "Notification counts retrieved successfully",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "Success Response",
                   value = """
                   {
                       "success": true,
                       "message": "Notification counts retrieved successfully",
                       "requestId": "req_123456789",
                       "data": {
                           "total": 25,
                           "unread": 5,
                           "read": 20,
                           "TRANSACTION": 10,
                           "SECURITY": 3,
                           "KYC": 2,
                           "SYSTEM": 8,
                           "PROMOTION": 2
                       },
                       "timestamp": "2024-01-15T13:00:00Z"
                   }
                   """
               )
           )
       ),
       @ApiResponse(
           responseCode = "400",
           description = "Invalid user ID",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse")
           )
       ),
       @ApiResponse(
           responseCode = "404",
           description = "User not found",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse")
           )
       )
   })
    @GetMapping("/users/{userId}/counts")
    public ResponseEntity<CustomApiResponse<Map<String, Long>>> getNotificationCounts(
            @Parameter(
                description = "Unique identifier of the user whose notification counts to retrieve",
                required = true,
                example = "user123"
            )
            @PathVariable String userId) {
        
        log.info("Retrieving notification counts for user: {}", userId);
        CustomApiResponse<Map<String, Long>> response = notificationService.getNotificationCounts(userId);
        
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Delete notification",
        description = "Permanently deletes a specific notification. This action cannot be undone and the notification will be removed from the user's notification history.",
        tags = {"Notifications"}
    )
   @ApiResponses(value = {
       @ApiResponse(
           responseCode = "200",
           description = "Notification deleted successfully",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "Success Response",
                   value = """
                   {
                       "success": true,
                       "message": "Notification deleted successfully",
                       "requestId": "req_123456789",
                       "data": "Notification notif_001 deleted successfully",
                       "timestamp": "2024-01-15T14:00:00Z"
                   }
                   """
               )
           )
       ),
       @ApiResponse(
           responseCode = "400",
           description = "Invalid notification ID or deletion failed",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse")
           )
       ),
       @ApiResponse(
           responseCode = "404",
           description = "Notification not found",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(ref = "#/components/schemas/ErrorResponse"),
               examples = @ExampleObject(
                   name = "Not Found Error",
                   value = """
                   {
                       "success": false,
                       "message": "Notification not found",
                       "requestId": "req_123456789",
                       "errors": ["Notification with ID notif_001 not found"],
                       "timestamp": "2024-01-15T14:00:00Z"
                   }
                   """
               )
           )
       )
   })
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<CustomApiResponse<String>> deleteNotification(
            @Parameter(
                description = "Unique identifier of the notification to delete",
                required = true,
                example = "notif_987654321"
            )
            @PathVariable String notificationId) {
        
        log.info("Deleting notification: {}", notificationId);
        CustomApiResponse<String> response = notificationService.deleteNotification(notificationId);
        
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
}
