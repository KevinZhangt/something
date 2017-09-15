#include <malloc.h>
#include <string.h>
#include <stdio.h>
#include <pthread.h>


struct ring_buffer {
    char* buf;
    volatile int head;
    volatile int tail;
    int size;
};


void ring_buffer_init(ring_buffer* rb, int size) {
    rb->buf = (char*) malloc(sizeof(char) * (size+1));
    rb->head = 0;
    rb->tail = 0;
    rb->size = size;
}

int ring_buffer_write(ring_buffer* rb, char* src, int size) {
    int remain = rb->head - rb->tail - 1;
    if(remain < 0) {
        remain = remain + rb->size + 1;
    }
    if(remain < size) {
        printf("%d %d %d %d %d\n", rb, rb->head, rb->tail, remain, size);
        return 0;
    }
    else {
        if(rb->size - rb->tail >= size) {
            memcpy(rb->buf + rb->tail + 1, src, size);
            rb->tail = rb->tail + size;
        }
        else {
            memcpy(rb->buf + rb->tail + 1, src, rb->size - rb->tail);
            memcpy(rb->buf, src + rb->size - rb->tail, size - rb->size + rb->tail);
            rb->tail = rb->tail + size - rb->size - 1;
        }
        return 1;
    }
}

int ring_buffer_read(ring_buffer* rb, char* dst, int size) {
    int occupy = rb->tail - rb->head;
    if(occupy < 0) {
        occupy = occupy + rb->size + 1;
    }
    if(occupy < size) {
        // printf("%d %d %d %d %d\n", rb, rb->head, rb->tail, occupy, size);
        return 0;
    }
    else {
        if(rb->size - rb->head >=size) {
            memcpy(dst, rb->buf + rb->head + 1, size);
            rb->head = rb->head + size;
        }
        else {
            memcpy(dst, rb->buf + rb->head + 1, rb->size - rb->head);
            memcpy(dst + rb->size - rb->head, rb->buf, size - rb->size + rb->head);
            rb->head = rb->head + size - rb->size - 1;
        }
        return 1;
    }
}

ring_buffer r1;
ring_buffer r2;

int n = 10000000;

void *fun1(void* args) {
    char in_buf[1];
    char out_buf[1];
    char cnt = 0;
    for(int i=0;i<n;++i) {
        ring_buffer_write(&r1, in_buf, 1);
        while(!ring_buffer_read(&r2, out_buf, 1));
    }
}

void *fun2(void* args) {
    char in_buf[1];
    char out_buf[1];
    char cnt = 0;
    for(int i=0;i<n;++i) {
        ring_buffer_write(&r2, in_buf, 1);
        while(!ring_buffer_read(&r1, out_buf, 1));
    }
}


int main() {
    ring_buffer_init(&r1, 100);
    ring_buffer_init(&r2, 100);
    pthread_t t1, t2;
    pthread_create(&t1, NULL, fun1, NULL);
    pthread_create(&t2, NULL, fun2, NULL);
    pthread_join(t1, NULL);
    pthread_join(t2, NULL);

}
