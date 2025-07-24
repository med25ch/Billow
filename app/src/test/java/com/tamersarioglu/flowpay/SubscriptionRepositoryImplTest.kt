package com.tamersarioglu.flowpay

import com.tamersarioglu.flowpay.data.database.BillingInterval
import com.tamersarioglu.flowpay.data.database.paymenthistory.PaymentHistoryDao
import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionCategory
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionDao
import com.tamersarioglu.flowpay.data.repository.SubscriptionRepositoryImpl
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.UUID


class SubscriptionRepositoryImplTest {

    private val subscriptionDao : SubscriptionDao = mockk()
    private val paymentHistoryDao : PaymentHistoryDao = mockk()
    private lateinit var repository : SubscriptionRepositoryImpl


    @Before
    fun setup(){
        repository = SubscriptionRepositoryImpl(subscriptionDao,paymentHistoryDao)
    }

    @Test
    fun `getSubscriptionById returns subscription when found`() = runTest {

        // prepare the Subscription
        val id = UUID.randomUUID().toString()
        val expected = Subscription(
            id = id,
            name = "Netflix",
            price = 15.99,
            currency = "USD",
            billingInterval = BillingInterval.MONTHLY,
            startDate = LocalDate.of(2024, 1, 1),
            nextBillingDate = LocalDate.of(2025, 7, 1),
            iconUrl = "https://example.com/netflix.png",
            category = SubscriptionCategory.ENTERTAINMENT,
            description = "Streaming service"
        )

        coEvery { subscriptionDao.getSubscriptionById(id) } returns expected

        // run the method under test
        val result = repository.getSubscriptionById(id)

        // Assert
        assertEquals(expected, result)
        coVerify { subscriptionDao.getSubscriptionById(id)  }
    }


    @Test
    fun `insertSubscription then returns it `() = runTest {

        // FYI : I'm testing that my repository function behaves correctly by calling the DAO method.
        // Nothing to assert

        // prepare the Subscription
        val id = UUID.randomUUID().toString()
        val expected = Subscription(
            id = id,
            name = "Netflix",
            price = 15.99,
            currency = "USD",
            billingInterval = BillingInterval.MONTHLY,
            startDate = LocalDate.of(2024, 1, 1),
            nextBillingDate = LocalDate.of(2025, 7, 1),
            iconUrl = "https://example.com/netflix.png",
            category = SubscriptionCategory.ENTERTAINMENT,
            description = "Streaming service"
        )


        coEvery { subscriptionDao.insertSubscription(expected) } just Runs

        // run the method under test
        repository.insertSubscription(expected)

        // Assert
        coVerify { subscriptionDao.insertSubscription(expected)  }
    }

    @Test
    fun `updateSubscription a Subscription then we verify the result `() = runTest {

        // FYI : I'm testing that my repository function behaves correctly by calling the DAO method.
        // Nothing to assert

        // prepare the Subscription
        val id = UUID.randomUUID().toString()
        val subscription = Subscription(
            id = id,
            name = "Netflix",
            price = 15.99,
            currency = "USD",
            billingInterval = BillingInterval.MONTHLY,
            startDate = LocalDate.of(2024, 1, 1),
            nextBillingDate = LocalDate.of(2025, 7, 1),
            iconUrl = "https://example.com/netflix.png",
            category = SubscriptionCategory.ENTERTAINMENT,
            description = "Streaming service"
        )

        coEvery { subscriptionDao.updateSubscription(subscription) } just Runs

        // run the method under test
        repository.updateSubscription(subscription)

        // Assert
        coVerify(exactly = 1) { subscriptionDao.updateSubscription(subscription)  }
    }



}