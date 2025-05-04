const axios = require('axios');

// Configuration
const API_BASE_URL = 'http://localhost:8080/api';
const API_TIMEOUT = 5000;

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Test results tracking
const testResults = {
  passed: 0,
  failed: 0,
  skipped: 0,
  total: 0,
  details: []
};

// User credentials for different roles
const users = {
  admin: {
    email: 'admin@planbnext.com',
    password: 'Admin@123',
    token: null
  },
  expert: {
    email: 'expert@planbnext.com',
    password: 'Expert@123',
    token: null
  },
  user: {
    email: 'user@planbnext.com',
    password: 'User@123',
    token: null
  },
  support: {
    email: 'support@planbnext.com',
    password: 'Support@123',
    token: null
  }
};

// Helper function to run a test
async function runTest(name, testFn) {
  testResults.total++;
  console.log(`\n🧪 Running test: ${name}`);
  
  try {
    await testFn();
    testResults.passed++;
    testResults.details.push({ name, status: 'PASSED' });
    console.log(`✅ Test passed: ${name}`);
  } catch (error) {
    testResults.failed++;
    const errorMessage = error.response ? 
      `${error.response.status} - ${JSON.stringify(error.response.data)}` : 
      error.message;
    
    testResults.details.push({ 
      name, 
      status: 'FAILED', 
      error: errorMessage 
    });
    
    console.error(`❌ Test failed: ${name}`);
    console.error(`   Error: ${errorMessage}`);
  }
}

// Helper function to skip a test
function skipTest(name, reason) {
  testResults.total++;
  testResults.skipped++;
  testResults.details.push({ 
    name, 
    status: 'SKIPPED', 
    reason 
  });
  
  console.log(`\n⏭️  Skipping test: ${name}`);
  console.log(`   Reason: ${reason}`);
}

// Test suite
async function runTests() {
  console.log('🚀 Starting RBAC Tests');
  console.log('====================');
  
  // 1. Health Check
  await runTest('Health Check', async () => {
    const response = await api.get('/public/api/health');
    if (response.data.status !== 'UP') {
      throw new Error('Health check failed');
    }
  });
  
  // 2. User Registration Tests
  await runTest('Register Admin User', async () => {
    try {
      const userData = {
        email: users.admin.email,
        password: users.admin.password,
        displayName: 'Admin User',
        firstName: 'Admin',
        lastName: 'User',
        mobileNumber: '1234567890',
        country: 'US',
        customerType: 'User'
      };
      
      const response = await api.post('/auth/jwt/signup', userData);
      console.log('Admin user registered successfully');
    } catch (error) {
      if (error.response && error.response.status === 400 && 
          error.response.data.error === 'Email already in use') {
        console.log('Admin user already exists, continuing with tests');
      } else {
        throw error;
      }
    }
  });
  
  await runTest('Register Expert User', async () => {
    try {
      const userData = {
        email: users.expert.email,
        password: users.expert.password,
        displayName: 'Expert User',
        firstName: 'Expert',
        lastName: 'User',
        mobileNumber: '1234567891',
        country: 'US',
        customerType: 'User'
      };
      
      const response = await api.post('/auth/jwt/signup', userData);
      console.log('Expert user registered successfully');
    } catch (error) {
      if (error.response && error.response.status === 400 && 
          error.response.data.error === 'Email already in use') {
        console.log('Expert user already exists, continuing with tests');
      } else {
        throw error;
      }
    }
  });
  
  await runTest('Register Regular User', async () => {
    try {
      const userData = {
        email: users.user.email,
        password: users.user.password,
        displayName: 'Regular User',
        firstName: 'Regular',
        lastName: 'User',
        mobileNumber: '1234567892',
        country: 'US',
        customerType: 'User'
      };
      
      const response = await api.post('/auth/jwt/signup', userData);
      console.log('Regular user registered successfully');
    } catch (error) {
      if (error.response && error.response.status === 400 && 
          error.response.data.error === 'Email already in use') {
        console.log('Regular user already exists, continuing with tests');
      } else {
        throw error;
      }
    }
  });
  
  await runTest('Register Support User', async () => {
    try {
      const userData = {
        email: users.support.email,
        password: users.support.password,
        displayName: 'Support User',
        firstName: 'Support',
        lastName: 'User',
        mobileNumber: '1234567893',
        country: 'US',
        customerType: 'User'
      };
      
      const response = await api.post('/auth/jwt/signup', userData);
      console.log('Support user registered successfully');
    } catch (error) {
      if (error.response && error.response.status === 400 && 
          error.response.data.error === 'Email already in use') {
        console.log('Support user already exists, continuing with tests');
      } else {
        throw error;
      }
    }
  });
  
  // 3. User Login Tests
  await runTest('Admin Login', async () => {
    const loginData = {
      email: users.admin.email,
      password: users.admin.password
    };
    
    const response = await api.post('/auth/jwt/login', loginData);
    users.admin.token = response.data.token;
    
    if (!users.admin.token) {
      throw new Error('No auth token received for admin');
    }
  });
  
  await runTest('Expert Login', async () => {
    const loginData = {
      email: users.expert.email,
      password: users.expert.password
    };
    
    const response = await api.post('/auth/jwt/login', loginData);
    users.expert.token = response.data.token;
    
    if (!users.expert.token) {
      throw new Error('No auth token received for expert');
    }
  });
  
  await runTest('Regular User Login', async () => {
    const loginData = {
      email: users.user.email,
      password: users.user.password
    };
    
    const response = await api.post('/auth/jwt/login', loginData);
    users.user.token = response.data.token;
    
    if (!users.user.token) {
      throw new Error('No auth token received for regular user');
    }
  });
  
  await runTest('Support User Login', async () => {
    const loginData = {
      email: users.support.email,
      password: users.support.password
    };
    
    const response = await api.post('/auth/jwt/login', loginData);
    users.support.token = response.data.token;
    
    if (!users.support.token) {
      throw new Error('No auth token received for support user');
    }
  });
  
  // 4. Role Assignment Tests
  await runTest('Assign Admin Role', async () => {
    // Set admin token for authorization
    api.defaults.headers.common['Authorization'] = `Bearer ${users.admin.token}`;
    
    // Get user ID from token (this would require a separate endpoint)
    // For now, we'll assume we have the user ID
    const userId = '00000000-0000-0000-0000-000000000000'; // Replace with actual user ID
    
    // Assign admin role
    const response = await api.post(`/api/admin/users/${userId}/roles`, {
      role: 'ADMIN'
    });
    
    if (!response.data.success) {
      throw new Error('Failed to assign admin role');
    }
  });
  
  await runTest('Assign Expert Role', async () => {
    // Set admin token for authorization
    api.defaults.headers.common['Authorization'] = `Bearer ${users.admin.token}`;
    
    // Get user ID from token (this would require a separate endpoint)
    // For now, we'll assume we have the user ID
    const userId = '00000000-0000-0000-0000-000000000000'; // Replace with actual user ID
    
    // Assign expert role
    const response = await api.post(`/api/admin/users/${userId}/roles`, {
      role: 'EXPERT'
    });
    
    if (!response.data.success) {
      throw new Error('Failed to assign expert role');
    }
  });
  
  // 5. Role-Based Access Tests
  
  // 5.1 Admin Access Tests
  await runTest('Admin Access to Admin API', async () => {
    api.defaults.headers.common['Authorization'] = `Bearer ${users.admin.token}`;
    
    const response = await api.get('/api/admin/users');
    
    if (!response.data) {
      throw new Error('Admin could not access admin API');
    }
  });
  
  // 5.2 Expert Access Tests
  await runTest('Expert Access to Expert API', async () => {
    api.defaults.headers.common['Authorization'] = `Bearer ${users.expert.token}`;
    
    const response = await api.get('/api/expert/profile');
    
    if (!response.data) {
      throw new Error('Expert could not access expert API');
    }
  });
  
  await runTest('Expert Access to Admin API (Should Fail)', async () => {
    api.defaults.headers.common['Authorization'] = `Bearer ${users.expert.token}`;
    
    try {
      await api.get('/api/admin/users');
      throw new Error('Expert should not be able to access admin API');
    } catch (error) {
      if (error.response && error.response.status === 403) {
        // This is expected - access should be forbidden
        return;
      }
      throw error;
    }
  });
  
  // 5.3 Regular User Access Tests
  await runTest('User Access to User API', async () => {
    api.defaults.headers.common['Authorization'] = `Bearer ${users.user.token}`;
    
    const response = await api.get('/api/user/profile');
    
    if (!response.data) {
      throw new Error('User could not access user API');
    }
  });
  
  await runTest('User Access to Expert API (Should Fail)', async () => {
    api.defaults.headers.common['Authorization'] = `Bearer ${users.user.token}`;
    
    try {
      await api.get('/api/expert/profile');
      throw new Error('User should not be able to access expert API');
    } catch (error) {
      if (error.response && error.response.status === 403) {
        // This is expected - access should be forbidden
        return;
      }
      throw error;
    }
  });
  
  // 5.4 Support Access Tests
  await runTest('Support Access to Support API', async () => {
    api.defaults.headers.common['Authorization'] = `Bearer ${users.support.token}`;
    
    const response = await api.get('/api/support/tickets');
    
    if (!response.data) {
      throw new Error('Support could not access support API');
    }
  });
  
  await runTest('Support Access to Admin API (Should Fail)', async () => {
    api.defaults.headers.common['Authorization'] = `Bearer ${users.support.token}`;
    
    try {
      await api.get('/api/admin/users');
      throw new Error('Support should not be able to access admin API');
    } catch (error) {
      if (error.response && error.response.status === 403) {
        // This is expected - access should be forbidden
        return;
      }
      throw error;
    }
  });
  
  // 6. Feature-Based Access Tests
  
  // 6.1 Ticket Management Tests
  await runTest('User Create Ticket', async () => {
    api.defaults.headers.common['Authorization'] = `Bearer ${users.user.token}`;
    
    const ticketData = {
      title: 'Test Ticket',
      description: 'This is a test ticket',
      priority: 'MEDIUM',
      classification: 'SOFTWARE',
      area: 'BACKEND'
    };
    
    const response = await api.post('/api/tickets', ticketData);
    
    if (!response.data.id) {
      throw new Error('User could not create ticket');
    }
  });
  
  await runTest('Expert Pick Ticket', async () => {
    api.defaults.headers.common['Authorization'] = `Bearer ${users.expert.token}`;
    
    // Get a ticket ID (this would require a separate endpoint)
    // For now, we'll assume we have a ticket ID
    const ticketId = '00000000-0000-0000-0000-000000000000'; // Replace with actual ticket ID
    
    const response = await api.post(`/api/tickets/pick/${ticketId}`);
    
    if (!response.data.success) {
      throw new Error('Expert could not pick ticket');
    }
  });
  
  await runTest('User Pick Ticket (Should Fail)', async () => {
    api.defaults.headers.common['Authorization'] = `Bearer ${users.user.token}`;
    
    // Get a ticket ID (this would require a separate endpoint)
    // For now, we'll assume we have a ticket ID
    const ticketId = '00000000-0000-0000-0000-000000000000'; // Replace with actual ticket ID
    
    try {
      await api.post(`/api/tickets/pick/${ticketId}`);
      throw new Error('User should not be able to pick ticket');
    } catch (error) {
      if (error.response && error.response.status === 403) {
        // This is expected - access should be forbidden
        return;
      }
      throw error;
    }
  });
  
  // Print test summary
  console.log('\n====================');
  console.log('📊 Test Summary');
  console.log('====================');
  console.log(`Total: ${testResults.total}`);
  console.log(`Passed: ${testResults.passed}`);
  console.log(`Failed: ${testResults.failed}`);
  console.log(`Skipped: ${testResults.skipped}`);
  console.log('====================');
  
  if (testResults.failed > 0) {
    console.log('\n❌ Failed Tests:');
    testResults.details
      .filter(test => test.status === 'FAILED')
      .forEach(test => {
        console.log(`- ${test.name}: ${test.error}`);
      });
  }
}

// Run the tests
runTests().catch(error => {
  console.error('Error running tests:', error);
});
