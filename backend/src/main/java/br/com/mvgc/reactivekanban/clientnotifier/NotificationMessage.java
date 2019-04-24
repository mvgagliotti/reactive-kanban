package br.com.mvgc.reactivekanban.clientnotifier;

import lombok.Data;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collection;

@Data
@ToString
public class NotificationMessage<T> {

    private final NotificationType notificationType;
    private final Collection<T> objects;

    public NotificationMessage(NotificationType notificationType, T...objects) {
        this.notificationType = notificationType;
        this.objects = Arrays.asList(objects);
    }

}
