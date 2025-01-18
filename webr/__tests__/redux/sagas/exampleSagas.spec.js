import { put } from 'redux-saga/effects'
import { fetchMainData } from '../../../src/js/redux/sagas/mainSaga'
import { actions as exampleActions } from '../../../src/js/redux/modules/main'

describe('redux.sagas.exampleSaga', () => {
  describe('fetchMainData', () => {

    global.__CONFIG__ = {
      description: 'fake description'
    }

    const fixture = {
        title: 'Everything is Awesome',
        description: __CONFIG__.description,
        source: 'This message is coming from Redux',
      };

    it('should call exampleActions.updateExample with correct data', () => {
      const generator = fetchMainData()

      let next = generator.next()

      expect(next.value).toEqual(put(exampleActions.updateExample(fixture)))
    })
  })
})
