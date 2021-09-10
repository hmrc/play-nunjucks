const { mergeDeep } = require('../src/main/resources/uk/gov/hmrc/nunjucks/mergeDeep.js')

describe('mergeDeep', () => {
    it('should merge two simple Javascript dictionaries', () => {
        let original = {
            foo: 'bar'
        }
        let batch = {
            bar: 'baz'
        }

        let merged = mergeDeep(original, batch)

        expect(merged).toEqual({
            foo: 'bar',
            bar: 'baz'
        })
    })

    it('should merge two simple Javascript dictionaries with the second argument overwriting keys in the first', () => {
        let original = {
            foo: 'bar'
        }
        let batch = {
            foo: 'baz'
        }

        let merged = mergeDeep(original, batch)

        expect(merged).toEqual({
            foo: 'baz',
        })
    })

    it('should merge two simple Javascript dictionaries with the second argument overwriting keys in the first', () => {
        let original = {
            foo: {
                baz: 'bar',
                bam: 'boo'
            }
        }
        let batch = {
            foo: {
                dam: 'baz'
            }
        }

        let merged = mergeDeep(original, batch)

        expect(merged).toEqual({
            foo: {
                baz: 'bar',
                bam: 'boo',
                dam: 'baz'
            },
        })
    })
})
