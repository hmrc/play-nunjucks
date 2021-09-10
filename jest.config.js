module.exports = {
  modulePathIgnorePatterns: ['<rootDir>/node_modules/'],
  moduleFileExtensions: ['js'],
  testPathIgnorePatterns: ['/js/node_modules/', '/target'],
  coverageThreshold: {
    global: {
      branches: 100,
      functions: 100,
      lines: 100,
      statements: 100,
    },
  },
};
