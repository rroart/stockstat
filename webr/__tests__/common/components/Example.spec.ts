import React from 'react'
import { fromJS } from 'immutable'
import {render, screen} from '@testing-library/react'
import '@testing-library/jest-dom'

import { Main } from  '../../../src/js/common/components/Main'

const fixture = {
  example: {
    result: fromJS({
      testing: 'data',
    }),
  },
};

describe('ExampleView', () => {
  it('should render a blank div without data', () => {
    /*render(<Main />)
  
    expect(screen.length).toEqual(1)*/
    //expect(el.find('.exampleOutput').length).toEqual(0)
  })

  it('should render with correct data', () => {
    /*render(<Main {...fixture} />)

    expect(screen.length).toEqual(1)
    expect(screen.find('.exampleOutput').length).toEqual(1)*/
    //expect(el.length).toEqual(1)
    //expect(el.find('.exampleOutput').length).toEqual(1)
  })
})
