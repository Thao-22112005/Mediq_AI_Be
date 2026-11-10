package com.mediq.scheduling.service;

import com.mediq.scheduling.dto.response.SlotResponse;
import com.mediq.scheduling.entity.Slot;
import com.mediq.scheduling.entity.SlotStatus;
import com.mediq.scheduling.entity.WorkSchedule;
import com.mediq.scheduling.exception.BusinessException;
import com.mediq.scheduling.exception.ErrorCode;
import com.mediq.scheduling.mapper.SlotMapper;
import com.mediq.scheduling.repository.SlotRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotConcurrencyTest {

    @Test
    @DisplayName("Concurrent holdSlot calls on the same slot: exactly one succeeds, others fail with SLOT_ALREADY_HELD")
    void concurrentHoldSlot_OnlyOneSucceeds() throws InterruptedException {
        SlotRepository slotRepository = mock(SlotRepository.class);
        SlotMapper slotMapper = mock(SlotMapper.class);
        when(slotMapper.toSlotResponse(any(Slot.class))).thenReturn(mock(SlotResponse.class));

        SlotServiceImpl slotService = new SlotServiceImpl(slotRepository, slotMapper);

        UUID slotId = UUID.randomUUID();
        WorkSchedule schedule = WorkSchedule.builder()
                .scheduleId(UUID.randomUUID())
                .date(LocalDate.now().plusDays(1))
                .build();

        Slot targetSlot = Slot.builder()
                .slotId(slotId)
                .workSchedule(schedule)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .status(SlotStatus.AVAILABLE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // Simulate DB pessimistic write lock with a ReentrantLock
        ReentrantLock dbPessimisticLock = new ReentrantLock();

        when(slotRepository.findByIdWithLock(slotId)).thenAnswer(invocation -> {
            dbPessimisticLock.lock();
            return Optional.of(targetSlot);
        });

        when(slotRepository.save(any(Slot.class))).thenAnswer(invocation -> {
            try {
                return targetSlot;
            } finally {
                if (dbPessimisticLock.isHeldByCurrentThread()) {
                    dbPessimisticLock.unlock();
                }
            }
        });

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger notAvailableCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // wait until all threads are ready to fire simultaneously
                    SlotResponse res = slotService.holdSlot(slotId);
                    if (res != null) {
                        successCount.incrementAndGet();
                    }
                } catch (BusinessException e) {
                    if (e.getErrorCode() == ErrorCode.SLOT_NOT_AVAILABLE) {
                        notAvailableCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // unexpected
                } finally {
                    if (dbPessimisticLock.isHeldByCurrentThread()) {
                        dbPessimisticLock.unlock();
                    }
                    endLatch.countDown();
                }
            });
        }

        // Trigger all threads simultaneously
        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(notAvailableCount.get()).isEqualTo(threadCount - 1);
        assertThat(targetSlot.getStatus()).isEqualTo(SlotStatus.HELD);
    }
}
