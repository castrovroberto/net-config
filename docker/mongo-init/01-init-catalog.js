// Initialize the catalog database with collections and indexes
db = db.getSiblingDB('catalog');

// Create products collection with indexes
db.createCollection('products');
db.products.createIndex({ "sku": 1 }, { unique: true });
db.products.createIndex({ "type": 1 });
db.products.createIndex({ "attributes.ports": 1 });

print('MongoDB catalog database initialized successfully');

